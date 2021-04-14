package se.magnus.microservices.core.product.services;

import javax.annotation.Generated;
import org.springframework.stereotype.Component;
import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.persistence.ProductEntity;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-04-13T17:20:01-0400",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 1.8.0_282 (AdoptOpenJDK)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product entityToApi(ProductEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Product product = new Product();

        product.setProductId( entity.getProductId() );
        product.setName( entity.getName() );
        product.setWeight( entity.getWeight() );

        return product;
    }

    @Override
    public ProductEntity apiToEntity(Product api) {
        if ( api == null ) {
            return null;
        }

        ProductEntity productEntity = new ProductEntity();

        productEntity.setProductId( api.getProductId() );
        productEntity.setName( api.getName() );
        productEntity.setWeight( api.getWeight() );

        return productEntity;
    }
}
