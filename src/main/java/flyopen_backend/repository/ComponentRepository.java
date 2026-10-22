package flyopen_backend.repository;

import flyopen_backend.entity.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Long> {

        Optional<Component> findByReference(String reference);
}
