package com.example.dlg.domain;

import java.io.Serializable;
import javax.persistence.*;

/**
 * A Orders.
 */
@Entity
@Table(name = "orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "total")
    private Integer total;

    @Column(name = "quantity")
    private Long quantity;

    @ManyToOne
    private Products products;

    @ManyToOne
    private People people;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Orders id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTotal() {
        return this.total;
    }

    public Orders total(Integer total) {
        this.setTotal(total);
        return this;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Long getQuantity() {
        return this.quantity;
    }

    public Orders quantity(Long quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Products getProducts() {
        return this.products;
    }

    public void setProducts(Products products) {
        this.products = products;
    }

    public Orders products(Products products) {
        this.setProducts(products);
        return this;
    }

    public People getPeople() {
        return this.people;
    }

    public void setPeople(People people) {
        this.people = people;
    }

    public Orders people(People people) {
        this.setPeople(people);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Orders)) {
            return false;
        }
        return id != null && id.equals(((Orders) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Orders{" +
            "id=" + getId() +
            ", total=" + getTotal() +
            ", quantity=" + getQuantity() +
            "}";
    }
}
