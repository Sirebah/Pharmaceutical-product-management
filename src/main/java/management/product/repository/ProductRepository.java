package management.product.repository;

import management.product.domain.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Produit, Long> {


}
