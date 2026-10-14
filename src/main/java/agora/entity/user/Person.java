package agora.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "person")
public class Person extends User {

    public Person() {}

    @Column(name = "name")
    private String name;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate birthDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String beautify() {
        return String.format("[PERSON] \n    ID: %d;\n    Логин: %s;\n    Имя: %s;\n    Фамилия: %s;\n    Дата рождения: %s;\n",
                super.getId(), super.getLogin(), this.name, this.lastName, this.birthDate);
    }

    @Override
    public String userName() {
        return "[PERSON] " + this.name + " " + this.lastName;
    }

    @Override
    public String toFile() {
        DateTimeFormatter fDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter fDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String birthDateString = this.birthDate.format(fDate);
        String registrationDateString = this.registrationDate.format(fDateTime);
        return String.format("{PERSON}{%d}{%s}{%s}{%s}{%s}{%s}{%s}", super.getId(), super.getLogin(), super.getPasswordHash(), this.name, this.lastName, birthDateString, registrationDateString);
    }
}
