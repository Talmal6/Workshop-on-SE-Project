package com.SEGroup.acceptance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Domain.ProductCatalog.InMemoryProductCatalog;
import com.SEGroup.Domain.ProductCatalog.ProductCatalog;
import com.SEGroup.Domain.Store.StoreRepository;
import com.SEGroup.Domain.User.UserRepository;
import com.SEGroup.Infrastructure.IAuthenticationService;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;

public class ProductSearchAndUpdatingAcceptanceTests {

    private static final String VALID_SESSION = "valid-session";
    private static final String OWNER_EMAIL = "owner@example.com";

    StoreService storeService;
    StoreRepository storeRepository;
    ProductCatalog productCatalog;
    IAuthenticationService authenticationService;
    IUserRepository userRepository;

    @BeforeEach
    public void setUp() throws Exception {
        storeRepository = new StoreRepository();
        productCatalog = new InMemoryProductCatalog();
        authenticationService = mock(IAuthenticationService.class);
        userRepository = mock(UserRepository.class);
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository);

        // Basic authentication stubs
        lenient().doNothing().when(authenticationService).checkSessionKey(VALID_SESSION);
        lenient().when(authenticationService.authenticate(VALID_SESSION)).thenReturn(VALID_SESSION);
        lenient().when(authenticationService.getUserBySession(VALID_SESSION)).thenReturn(OWNER_EMAIL);

        setupSampleCatalog();
        setupSampleStores();
    }

    private void setupSampleCatalog() throws Exception {
        productCatalog.addCatalogProduct("iphone13", "iPhone 13", "Apple", "Latest iPhone", Arrays.asList("phones", "electronics"));
        productCatalog.addCatalogProduct("macbookair", "Macbook Air", "Apple", "M2 Chip", Arrays.asList("laptops", "electronics"));
        productCatalog.addCatalogProduct("pixel7", "Pixel 7", "Google", "Android flagship", Arrays.asList("phones"));
        productCatalog.addCatalogProduct("galaxys23", "Galaxy S23", "Samsung", "Android premium", Arrays.asList("phones"));
        productCatalog.addCatalogProduct("ipadpro", "iPad Pro", "Apple", "12.9 inch Tablet", Arrays.asList("tablets", "electronics"));
        productCatalog.addCatalogProduct("surfacepro9", "Surface Pro 9", "Microsoft", "Tablet and Laptop hybrid", Arrays.asList("tablets", "laptops"));
        productCatalog.addCatalogProduct("dellxps13", "Dell XPS 13", "Dell", "Premium Windows laptop", Arrays.asList("laptops"));
        productCatalog.addCatalogProduct("galaxyzfold4", "Galaxy Z Fold 4", "Samsung", "Folding phone innovation", Arrays.asList("phones"));
        productCatalog.addCatalogProduct("rogphone6", "ROG Phone 6", "Asus", "Gaming smartphone", Arrays.asList("phones", "gaming"));
        productCatalog.addCatalogProduct("nintendoswitch", "Nintendo Switch", "Nintendo", "Hybrid gaming console", Arrays.asList("gaming", "electronics"));
        productCatalog.addCatalogProduct("ps5", "PlayStation 5", "Sony", "Next-gen gaming console", Arrays.asList("gaming"));
        productCatalog.addCatalogProduct("xboxseriesx", "Xbox Series X", "Microsoft", "High-end gaming console", Arrays.asList("gaming"));
        productCatalog.addCatalogProduct("kindlepw", "Kindle Paperwhite", "Amazon", "E-ink ebook reader", Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("fitbitcharge5", "Fitbit Charge 5", "Fitbit", "Fitness tracker", Arrays.asList("electronics", "fitness"));
        productCatalog.addCatalogProduct("garminfenix7", "Garmin Fenix 7", "Garmin", "Premium sports smartwatch", Arrays.asList("electronics", "fitness"));
        productCatalog.addCatalogProduct("dysonv15", "Dyson V15", "Dyson", "Cordless vacuum cleaner", Arrays.asList("home", "electronics"));
        productCatalog.addCatalogProduct("iphonese", "iPhone SE", "Apple", "Budget iPhone model", Arrays.asList("phones", "electronics"));
        productCatalog.addCatalogProduct("galaxya54", "Galaxy A54", "Samsung", "Mid-range Android", Arrays.asList("phones"));
        productCatalog.addCatalogProduct("xiaomiredminote12", "Redmi Note 12", "Xiaomi", "Affordable Android phone", Arrays.asList("phones"));
        productCatalog.addCatalogProduct("lenovothinkpadx1", "ThinkPad X1 Carbon", "Lenovo", "Business ultrabook", Arrays.asList("laptops"));
        productCatalog.addCatalogProduct("hpomen16", "HP Omen 16", "HP", "Gaming laptop", Arrays.asList("laptops", "gaming"));
        productCatalog.addCatalogProduct("asuszenbook", "Asus Zenbook 14", "Asus", "Ultralight laptop", Arrays.asList("laptops"));
        productCatalog.addCatalogProduct("macstudio", "Mac Studio", "Apple", "High-end desktop for creators", Arrays.asList("desktops", "electronics"));
        productCatalog.addCatalogProduct("chromecast4k", "Chromecast 4K", "Google", "Streaming media device", Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("boseqc45", "Bose QuietComfort 45", "Bose", "Noise-cancelling headphones", Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("sonywh1000xm5", "Sony WH-1000XM5", "Sony", "Flagship noise-cancelling headphones", Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("logitechmxkeys", "Logitech MX Keys", "Logitech", "Premium keyboard", Arrays.asList("electronics", "accessories"));
        productCatalog.addCatalogProduct("logitechmxmaster3s", "Logitech MX Master 3S", "Logitech", "High-end productivity mouse", Arrays.asList("electronics", "accessories"));
        productCatalog.addCatalogProduct("canonr6", "Canon EOS R6", "Canon", "Professional mirrorless camera", Arrays.asList("electronics", "photography"));
        productCatalog.addCatalogProduct("nikonz9", "Nikon Z9", "Nikon", "Flagship mirrorless camera", Arrays.asList("electronics", "photography"));
    }

    private void setupSampleStores() throws Exception {
        storeService.createStore(VALID_SESSION, "TechHeaven");
        storeService.createStore(VALID_SESSION, "GadgetWorld");
        storeService.createStore(VALID_SESSION, "GamingHub");


        storeService.addProductToStore(VALID_SESSION, "TechHeaven", "iphone13", "iPhone 13 128GB", "Latest iPhone", 999.99, 5);
        storeService.addProductToStore(VALID_SESSION, "TechHeaven", "macbookair", "Macbook Air Used", "M2 Chip", 1299.99, 3);
        storeService.addProductToStore(VALID_SESSION, "GadgetWorld", "pixel7", "Pixel 7 64GB 15MP Camera", "Android flagship", 599.99, 10);
        storeService.addProductToStore(VALID_SESSION, "GadgetWorld", "galaxya54", "Galaxy A54", "Android flagship", 549.99, 10);
        storeService.addProductToStore(VALID_SESSION, "GamingHub", "ps5", "PlayStation 5", "Next-gen console", 499.99, 8);
        storeService.addProductToStore(VALID_SESSION, "GamingHub", "xboxseriesx", "Xbox Series X", "Gaming console", 499.99, 7);
    }

    @Test
    public void searchAllProducts_ShouldReturnMultipleResults() {
        Result<List<ShoppingProductDTO>> result = storeService.searchProducts("Galaxy", Collections.emptyList(), null, null);
        assertTrue(result.isSuccess());
        assertFalse(result.getData().isEmpty());

        List<String> names = result.getData().stream().map(ShoppingProductDTO::getName).collect(Collectors.toList());
        assertTrue(names.stream().anyMatch(name -> name.contains("Galaxy")));
    }

    @Test
    public void searchProducts_WithPriceFilter_ShouldReturnOnlyCheapItems() {
        Result<List<ShoppingProductDTO>> result = storeService.searchProducts("PlayStation", Collections.singletonList("price<600"), null, null);
        assertTrue(result.isSuccess());
        assertFalse(result.getData().isEmpty());

        for (ShoppingProductDTO dto : result.getData()) {
            assertTrue(dto.getPrice() < 600);
        }
    }

    @Test
    public void searchProducts_ByStoreName_ShouldRestrictResults() {
        Result<List<ShoppingProductDTO>> result = storeService.searchProducts("Macbook", Collections.emptyList(), "TechHeaven", null);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("TechHeaven", result.getData().get(0).getStoreName());
    }

    @Test
    public void searchProducts_ByCategory_ShouldOnlyReturnPhones() {
        Result<List<ShoppingProductDTO>> result = storeService.searchProducts("", Collections.emptyList(), null, Collections.singletonList("phones"));
        assertTrue(result.isSuccess());
        assertFalse(result.getData().isEmpty());
    }
}