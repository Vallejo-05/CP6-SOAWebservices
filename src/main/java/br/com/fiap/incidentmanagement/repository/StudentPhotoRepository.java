package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.domain.student.StudentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentPhotoRepository extends JpaRepository<StudentPhoto, Long> {
}
