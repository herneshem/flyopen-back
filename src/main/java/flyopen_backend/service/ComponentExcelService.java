
package flyopen_backend.service;

import flyopen_backend.entity.Component;
import flyopen_backend.repository.ComponentRepository;

import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ComponentExcelService {

    private final ComponentRepository componentRepository;

    public ComponentExcelService(
            ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    // =========================
    // EXPORTAR EXCEL
    // =========================

    public byte[] exportComponents() throws IOException {

        List<Component> components = componentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Components");

            String[] headers = {
                    "reference", "name", "material", "value", "size",
                    "length_mm", "width_mm", "height_mm", "diameter_mm",
                    "weight_kg", "manufacturing_date", "stock",
                    "description"
            };

            Row header = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 18 * 256);
            }

            int rowIndex = 1;

            for (Component component : components) {
                Row row = sheet.createRow(rowIndex++);

                setText(row, 0, component.getReference());
                setText(row, 1, component.getName());
                setText(row, 2, component.getMaterial());
                setNumber(row, 3, component.getValue());
                setNumber(row, 4, component.getSize());
                setNumber(row, 5, component.getLengthMm());
                setNumber(row, 6, component.getWidthMm());
                setNumber(row, 7, component.getHeightMm());
                setNumber(row, 8, component.getDiameterMm());
                setNumber(row, 9, component.getWeightKg());

                if (component.getManufacturingDate() != null) {
                    row.createCell(10).setCellValue(
                            component.getManufacturingDate().toString()
                    );
                }

                if (component.getStock() != null) {
                    row.createCell(11).setCellValue(
                            component.getStock()
                    );
                }

                setText(row, 12, component.getDescription());
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0, Math.max(0, rowIndex - 1),
                            0, headers.length - 1
                    )
            );

            workbook.write(output);
            return output.toByteArray();
        }
    }

    private void setText(Row row, int column, String value) {
        if (value != null) {
            row.createCell(column).setCellValue(value);
        }
    }

    private void setNumber(
            Row row, int column, BigDecimal value) {
        if (value != null) {
            row.createCell(column).setCellValue(value.doubleValue());
        }
    }

    // =========================
    // IMPORTAR EXCEL
    // =========================

    @Transactional
    public int importComponents(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un archivo Excel."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new IllegalArgumentException(
                    "El archivo debe tener extensión .xlsx."
            );
        }

        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook =
                     new XSSFWorkbook(file.getInputStream())) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "El Excel no contiene hojas."
                );
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

// Comprobar que existe la primera fila y que no está vacía.
            if (headerRow == null || isRowEmpty(headerRow, formatter)) {
                throw new IllegalArgumentException(
                        "El Excel no contiene encabezados. " +
                                "Utiliza la plantilla de importación."
                );
            }

            // Leer los encabezados sin depender de su orden.
            Map<String, Integer> columns = new HashMap<>();

            for (Cell cell : headerRow) {
                String header = normalizeHeader(
                        formatter.formatCellValue(cell)
                );

                if (!header.isEmpty()) {
                    if (columns.containsKey(header)) {
                        throw new IllegalArgumentException(
                                "Encabezado duplicado: " + header
                        );
                    }

                    columns.put(header, cell.getColumnIndex());
                }
            }

            List<String> missingHeaders = new ArrayList<>();

            for (String required : List.of("reference", "name")) {
                if (!columns.containsKey(required)) {
                    missingHeaders.add(required);
                }
            }

            if (!missingHeaders.isEmpty()) {
                throw new IllegalArgumentException(
                        "The required headers are missing: "
                                + String.join(", ", missingHeaders)
                );
            }

            List<Component> components = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            Set<String> references = new HashSet<>();

            // Leer y validar todas las filas antes de guardar.
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }

                int excelRow = i + 1;

                try {
                    String reference = readText(
                            row, columns, "reference", formatter
                    );

                    String name = readText(
                            row, columns, "name", formatter
                    );

                    if (reference == null || reference.isBlank()) {
                        throw new IllegalArgumentException(
                                "reference es obligatorio."
                        );
                    }

                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException(
                                "name es obligatorio."
                        );
                    }

                    if (!references.add(reference)) {
                        throw new IllegalArgumentException(
                                "reference duplicado en el Excel: "
                                        + reference
                        );
                    }

                    Component component = new Component();

                    component.setReference(reference);
                    component.setName(name);

                    component.setMaterial(readText(
                            row, columns, "material", formatter
                    ));

                    component.setValue(readNumber(
                            row, columns, "value", formatter
                    ));

                    component.setSize(readNumber(
                            row, columns, "size", formatter
                    ));

                    component.setLengthMm(readNumber(
                            row, columns, "length_mm", formatter
                    ));

                    component.setWidthMm(readNumber(
                            row, columns, "width_mm", formatter
                    ));

                    component.setHeightMm(readNumber(
                            row, columns, "height_mm", formatter
                    ));

                    component.setDiameterMm(readNumber(
                            row, columns, "diameter_mm", formatter
                    ));

                    component.setWeightKg(readNumber(
                            row, columns, "weight_kg", formatter
                    ));

                    component.setManufacturingDate(readDate(
                            row, columns, "manufacturing_date", formatter
                    ));

                    component.setStock(readStock(
                            row, columns, formatter
                    ));

                    component.setDescription(readText(
                            row, columns, "description", formatter
                    ));

                    validateNonNegative(component);

                    components.add(component);

                } catch (RuntimeException e) {
                    errors.add(
                            "Fila " + excelRow + ": " + e.getMessage()
                    );
                }
            }

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException(
                        "Se ha leído el Excel, pero contiene errores:\n"
                                + String.join("\n", errors)
                );
            }

            if (components.isEmpty()) {
                throw new IllegalArgumentException(
                        "El Excel no contiene filas con datos."
                );
            }

            // Guardar solo cuando todas las filas sean válidas.
            for (Component component : components) {

                Optional<Component> existing =
                        componentRepository.findByReference(
                                component.getReference()
                        );

                if (existing.isPresent()) {
                    Component current = existing.get();
                    updateComponent(current, component);
                    componentRepository.save(current);
                } else {
                    componentRepository.save(component);
                }
            }

            return components.size();

        } catch (NotOfficeXmlFileException e) {
            throw new IllegalArgumentException(
                    "El archivo no es un Excel .xlsx válido."
            );
        }
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private String normalizeHeader(String header) {

        if (header == null) {
            return "";
        }

        String normalized = header.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "_");

        // Permitir encabezados habituales en español.
        return switch (normalized) {
            case "referencia" -> "reference";
            case "nombre" -> "name";
            case "valor", "precio" -> "value";
            case "material" -> "material";
            case "fecha_fabricacion" -> "manufacturing_date";
            case "longitud_mm" -> "length_mm";
            case "ancho_mm" -> "width_mm";
            case "altura_mm" -> "height_mm";
            case "diametro_mm" -> "diameter_mm";
            case "peso_kg" -> "weight_kg";
            case "descripcion" -> "description";
            case "existencias" -> "stock";
            case "tamano", "tamaño" -> "size";
            default -> normalized;
        };
    }

    private boolean isRowEmpty(
            Row row, DataFormatter formatter) {

        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String readText(
            Row row,
            Map<String, Integer> columns,
            String field,
            DataFormatter formatter) {

        Integer index = columns.get(field);

        if (index == null) {
            return null;
        }

        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        String value = formatter.formatCellValue(cell).trim();

        return value.isEmpty() ? null : value;
    }

    private BigDecimal readNumber(
            Row row,
            Map<String, Integer> columns,
            String field,
            DataFormatter formatter) {

        Integer index = columns.get(field);

        if (index == null) {
            return null;
        }

        Cell cell = row.getCell(index);

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        String value = formatter.formatCellValue(cell).trim();

        if (value.isEmpty()) {
            return null;
        }

        // Aceptar coma decimal cuando no hay punto decimal.
        if (value.contains(",") && !value.contains(".")) {
            value = value.replace(',', '.');
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    field + " debe contener un número válido."
            );
        }
    }

    private LocalDate readDate(
            Row row,
            Map<String, Integer> columns,
            String field,
            DataFormatter formatter) {

        Integer index = columns.get(field);

        if (index == null) {
            return null;
        }

        Cell cell = row.getCell(index);

        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        String value = formatter.formatCellValue(cell).trim();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    field + " debe tener formato AAAA-MM-DD."
            );
        }
    }

    private Integer readStock(
            Row row,
            Map<String, Integer> columns,
            DataFormatter formatter) {

        BigDecimal value = readNumber(
                row, columns, "stock", formatter
        );

        if (value == null) {
            return 0;
        }

        try {
            int stock = value.intValueExact();

            if (stock < 0) {
                throw new IllegalArgumentException(
                        "stock no puede ser negativo."
                );
            }

            return stock;

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "stock debe ser un entero válido."
            );
        }
    }

    private void validateNonNegative(Component component) {

        checkNonNegative("value", component.getValue());
        checkNonNegative("size", component.getSize());
        checkNonNegative("length_mm", component.getLengthMm());
        checkNonNegative("width_mm", component.getWidthMm());
        checkNonNegative("height_mm", component.getHeightMm());
        checkNonNegative("diameter_mm", component.getDiameterMm());
        checkNonNegative("weight_kg", component.getWeightKg());
    }

    private void checkNonNegative(
            String field, BigDecimal value) {

        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException(
                    field + " no puede ser negativo."
            );
        }
    }

    private void updateComponent(
            Component target, Component source) {

        target.setName(source.getName());
        target.setMaterial(source.getMaterial());
        target.setValue(source.getValue());
        target.setSize(source.getSize());
        target.setLengthMm(source.getLengthMm());
        target.setWidthMm(source.getWidthMm());
        target.setHeightMm(source.getHeightMm());
        target.setDiameterMm(source.getDiameterMm());
        target.setWeightKg(source.getWeightKg());
        target.setManufacturingDate(source.getManufacturingDate());
        target.setStock(source.getStock());
        target.setDescription(source.getDescription());
    }
}
