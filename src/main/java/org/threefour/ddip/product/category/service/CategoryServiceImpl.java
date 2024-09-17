package org.threefour.ddip.product.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threefour.ddip.product.category.domain.Category;
import org.threefour.ddip.product.category.domain.ConnectCategoryRequest;
import org.threefour.ddip.product.category.domain.ProductCategory;
import org.threefour.ddip.product.category.domain.RegisterCategoryRequest;
import org.threefour.ddip.product.category.exception.CategoryNotFoundException;
import org.threefour.ddip.product.category.repository.CategoryRepository;
import org.threefour.ddip.product.category.repository.ProductCategoryRepository;
import org.threefour.ddip.product.domain.Product;
import org.threefour.ddip.util.FormatConverter;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.*;
import static org.threefour.ddip.product.category.exception.ExceptionMessage.CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    @Override
    @Transactional(isolation = SERIALIZABLE, timeout = 30)
    public void createCategory(RegisterCategoryRequest registerCategoryRequest) {
        try {
            Category parentCategory
                    = getCategory(FormatConverter.parseToShort(registerCategoryRequest.getParentCategoryId()));
            categoryRepository.save(Category.from(registerCategoryRequest, parentCategory));
        } catch (CategoryNotFoundException cnfe) {
            categoryRepository.save(Category.from(registerCategoryRequest));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, timeout = 20)
    public void createProductCategories(ConnectCategoryRequest connectCategoryRequest, Product product) {
        Category firstCategory = getCategory(FormatConverter.parseToShort(connectCategoryRequest.getFirstCategoryId()));
        Category secondCategory
                = getCategory(FormatConverter.parseToShort(connectCategoryRequest.getSecondCategoryId()));
        Category thirdCategory = getCategory(FormatConverter.parseToShort(connectCategoryRequest.getThirdCategoryId()));

        for (Category category : List.of(firstCategory, secondCategory, thirdCategory)) {
            productCategoryRepository.save(ProductCategory.of(category, product));
        }
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 20)
    public List<Category> getCategories(Short parentCategoryId) {
        return categoryRepository.findByParentCategoryId(parentCategoryId);
    }

    @Override
    @Transactional(isolation = READ_UNCOMMITTED, readOnly = true, timeout = 10)
    public Category getCategory(Short categoryId) {
        return categoryRepository.findByIdAndDeleteYnFalse(categoryId).orElseThrow(
                () -> new CategoryNotFoundException(String.format(CATEGORY_NOT_FOUND_EXCEPTION_MESSAGE, categoryId))
        );
    }

    @Override
    public void deleteCategory(short id) {
        Category category = getCategory(id);
        category.delete();
        categoryRepository.save(category);
    }
}
