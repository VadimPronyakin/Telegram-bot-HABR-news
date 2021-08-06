package com.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@NoArgsConstructor
@Table(name = "Users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue
    private long id;
    private String userName;
    private long userTgId;

}
