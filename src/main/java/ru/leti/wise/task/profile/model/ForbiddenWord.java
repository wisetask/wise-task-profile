package ru.leti.wise.task.profile.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "forbidden_word")
public class ForbiddenWord {
    @Id
    private Long id;

    private String word;
}
