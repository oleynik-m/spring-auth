package com.example.dlg.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A Products.
 */
@Entity
@Table(name = "products")
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Products id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public Products title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return this.category;
    }

    public Products category(String category) {
        this.setCategory(category);
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Products price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Products)) {
            return false;
        }
        return id != null && id.equals(((Products) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Products{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", category='" + getCategory() + "'" +
            ", price=" + getPrice() +
            "}";
    }
}
