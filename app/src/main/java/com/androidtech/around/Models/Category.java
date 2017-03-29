package com.androidtech.around.Models;

import java.util.List;

/**
 * Created by OmarAli on 10/12/2016.
 */

public class Category {
    String fr_name;
    String icon;
    List<Specialization>specialization;

    public Category() {
    }

    public void setFr_name(String fr_name) {
        this.fr_name = fr_name;
    }

    public String getIcon() {
        return icon;
    }

    public String getFr_name() {
        return fr_name;
    }

    public List<Specialization> getSpecialization() {
        return specialization;
    }

    public void setSpecialization(List<Specialization> specialization) {
        this.specialization = specialization;
    }

    public List<Specialization> getmSpecializations() {
        return specialization;
    }
}
