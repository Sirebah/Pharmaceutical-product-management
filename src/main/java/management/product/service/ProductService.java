package management.product.service;

import management.product.domain.Produit;
import management.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class ProductService {
    @Autowired
    ProductRepository productRepository;
    public Produit create(Produit produit) {
        productRepository.save(produit);
        return produit;
    }
}
