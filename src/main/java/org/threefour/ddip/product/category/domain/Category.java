package org.threefour.ddip.product.category.domain;

import lombok.NoArgsConstructor;
import org.threefour.ddip.audit.BaseEntity;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;
import static org.threefour.ddip.util.EntityConstant.BOOLEAN_DEFAULT_FALSE;

@Entity
@NoArgsConstructor(access = PROTECTED)
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Short id;

    @Enumerated(EnumType.STRING)
    private CategoryName name;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @Column(nullable = false, columnDefinition = BOOLEAN_DEFAULT_FALSE)
    private boolean deleteYn;

    private Category(CategoryName name) {
        this.name = name;
    }

    private Category(CategoryName name, Category parentCategory) {
        this.name = name;
        this.parentCategory = parentCategory;
    }

    public static Category from(RegisterCategoryRequest registerCategoryRequest) {
        return new Category(registerCategoryRequest.getCategoryName());
    }

    public static Category from(RegisterCategoryRequest registerCategoryRequest, Category parentCategory) {
        return new Category(registerCategoryRequest.getCategoryName(), parentCategory);
    }

    Short getId() {
        return id;
    }

    CategoryName getName() {
        return name;
    }

    public void delete() {
        deleteYn = true;
    }
}
