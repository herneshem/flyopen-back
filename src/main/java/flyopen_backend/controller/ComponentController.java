package flyopen_backend.controller;

import flyopen_backend.service.ComponentExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private final ComponentExcelService componentExcelService;

    public ComponentController(ComponentExcelService componentExcelService){
        this.componentExcelService = componentExcelService;


    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportComponents() throws IOException {
        byte[] excel = componentExcelService.exportComponents();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=components.xlsx"
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(excel);
    }


    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> importComponents(
            @RequestParam("file") MultipartFile file) {

        try {
            int processed = componentExcelService.importComponents(file);

            return ResponseEntity.ok(
                    Map.of(
                            "mensaje", "Importación completada.",
                            "filasProcesadas", processed
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "No se pudo leer el archivo Excel.")
            );
        }
    }

}
