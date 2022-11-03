package com.mt.springbatch.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "converted_data")
@Getter
@Setter
public class ConvertedData {

    @Id
    /*
    @SequenceGenerator(name="identity_id_seq",sequenceName="identity_id_seq", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="identity_id_seq")
    @Column(unique=true, nullable=false)
    */
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    String firstName;

    String lastName;

    public ConvertedData(String firstName, String lastName){
        this.firstName= firstName;
        this.lastName = lastName;
    }

    public ConvertedData() {

    }
}
