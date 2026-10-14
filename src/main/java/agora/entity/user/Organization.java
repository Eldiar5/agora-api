package agora.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "organization")
public class Organization extends User{

    @Column(name = "date_of_foundation")
    private LocalDate dateOfFoundation;

    @Column(name = "title")
    private String title;

    @Column(name = "specialization")
    private String specialization;

    public Organization(LocalDate dateOfFoundation, String title, String specialization) {
        this.dateOfFoundation = dateOfFoundation;
        this.title = title;
        this.specialization = specialization;
    }

    public Organization() {}

    public LocalDate getDateOfFoundation() {
        return dateOfFoundation;
    }

    public void setDateOfFoundation(LocalDate dateOfFoundation) {
        this.dateOfFoundation = dateOfFoundation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public String beautify() {
        return String.format("[ORGANIZATION] \n    ID: %d;\n    Логин: %s;\n    Наименование: %s;\n    Род деятельности: %s;\n    Дата основания: %s;\n",
        super.getId(), super.getLogin(), this.title, this.specialization, this.dateOfFoundation);
    }

    @Override
    public String userName() {
        return "[ORGANIZATION] " + this.title;
    }

    @Override
    public String toFile() {
        DateTimeFormatter fFoundationDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter fDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateOfFoundationString = this.dateOfFoundation.format(fFoundationDate);
        String registrationString = this.registrationDate.format(fDateTime);
        return String.format("{ORGANIZATION}{%d}{%s}{%s}{%s}{%s}{%s}{%s}", super.getId(), super.getLogin(), super.getPasswordHash(), this.title, this.specialization, dateOfFoundationString, registrationString);
    }
}
