package com.example.demotddapp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Hoax {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
    private User user;
}
