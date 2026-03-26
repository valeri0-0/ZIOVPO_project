package com.valeria.license.repository;

import com.valeria.license.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// Используется для проверки существования продукта
public interface ProductRepository extends JpaRepository<Product, UUID> {
}