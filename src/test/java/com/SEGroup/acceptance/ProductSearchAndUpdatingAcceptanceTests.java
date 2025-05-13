package com.SEGroup.acceptance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.SEGroup.Domain.IAuthenticationService;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Repositories.*;
import com.SEGroup.Infrastructure.Security;
import com.SEGroup.Service.SecurityAdapter;
import com.SEGroup.Service.GuestService;
import com.SEGroup.Service.UserService;
import com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.Domain.IUserRepository;
import com.SEGroup.Service.Result;
import com.SEGroup.Service.StoreService;

import javax.crypto.SecretKey;

public class ProductSearchAndUpdatingAcceptanceTests {

    private static String VALID_SESSION = "valid-session";
    private static final String OWNER_EMAIL = "owner@example.com";

    UserService su;
    StoreService storeService;
    StoreRepository storeRepository;
    InMemoryProductCatalog productCatalog;
    IAuthenticationService authenticationService;
    IUserRepository userRepository;
    com.SEGroup.Infrastructure.NotificationCenter.NotificationCenter nc;

    @BeforeEach
    public void setUp() throws Exception {
        storeRepository = new StoreRepository();
        productCatalog = new InMemoryProductCatalog();

        // Create a Security instance
        Security security = new Security();
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        security.setKey(key);

        //
        userRepository = new UserRepository();
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository,new NotificationCenter(authenticationService));
        su = new UserService(new GuestService(new GuestRepository(), authenticationService), userRepository, authenticationService);
        // Create a shared PasswordEncoder for consistent usage



        // This line is redundant now that we're using constructor injection
        // ((SecurityAdapter)authenticationService).setPasswordEncoder(new com.SEGroup.Infrastructure.PasswordEncoder());

        PasswordEncoder passwordEncoder = new PasswordEncoder();
        authenticationService = new SecurityAdapter(security, passwordEncoder);
        userRepository = new UserRepository(passwordEncoder);
        storeService = new StoreService(storeRepository, productCatalog, authenticationService, userRepository,nc);

        // Add passwordEncoder parameter here
        su = new UserService(
                new GuestService(new GuestRepository(), authenticationService),
                userRepository,
                authenticationService,
                passwordEncoder  // Use the same passwordEncoder instance
        );

        VALID_SESSION = regLoginAndGetSession("owner", OWNER_EMAIL, "password");

        setupSampleCatalog();
        setupSampleStores();
    }

    public String regLoginAndGetSession(String userName, String email, String password) throws Exception {
        // Register a new user
        Result<Void> regResult = su.register(userName, email, password);
        // Authenticate the user and get a session key
        return  authenticationService.authenticate(email);
    }

    private void setupSampleCatalog() throws Exception {
        productCatalog.addCatalogProduct("iphone13", "iPhone 13", Arrays.asList("phones", "electronics"));
        productCatalog.addCatalogProduct("macbookair", "Macbook Air", Arrays.asList("laptops", "electronics"));
        productCatalog.addCatalogProduct("pixel7", "Pixel 7",  Arrays.asList("phones"));
        productCatalog.addCatalogProduct("galaxys23", "Galaxy S23",  Arrays.asList("phones"));
        productCatalog.addCatalogProduct("ipadpro", "iPad Pro", Arrays.asList("tablets", "electronics"));
        productCatalog.addCatalogProduct("surfacepro9", "Surface Pro 9", Arrays.asList("tablets", "laptops"));
        productCatalog.addCatalogProduct("dellxps13", "Dell XPS 13",  Arrays.asList("laptops"));
        productCatalog.addCatalogProduct("galaxyzfold4", "Galaxy Z Fold 4",  Arrays.asList("phones"));
        productCatalog.addCatalogProduct("rogphone6", "ROG Phone 6",  Arrays.asList("phones", "gaming"));
        productCatalog.addCatalogProduct("nintendoswitch", "Nintendo Switch", Arrays.asList("gaming", "electronics"));
        productCatalog.addCatalogProduct("ps5", "PlayStation 5",  Arrays.asList("gaming"));
        productCatalog.addCatalogProduct("xboxseriesx", "Xbox Series X",  Arrays.asList("gaming"));
        productCatalog.addCatalogProduct("kindlepw", "Kindle Paperwhite",  Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("fitbitcharge5", "Fitbit Charge 5",  Arrays.asList("electronics", "fitness"));
        productCatalog.addCatalogProduct("garminfenix7", "Garmin Fenix 7",  Arrays.asList("electronics", "fitness"));
        productCatalog.addCatalogProduct("dysonv15", "Dyson V15", Arrays.asList("home", "electronics"));
        productCatalog.addCatalogProduct("iphonese", "iPhone SE", Arrays.asList("phones", "electronics"));
        productCatalog.addCatalogProduct("galaxya54", "Galaxy A54",  Arrays.asList("phones"));
        productCatalog.addCatalogProduct("xiaomiredminote12", "Redmi Note 12",  Arrays.asList("phones"));
        productCatalog.addCatalogProduct("lenovothinkpadx1", "ThinkPad X1 Carbon",  Arrays.asList("laptops"));
        productCatalog.addCatalogProduct("hpomen16", "HP Omen 16",  Arrays.asList("laptops", "gaming"));
        productCatalog.addCatalogProduct("asuszenbook", "Asus Zenbook 14", Arrays.asList("laptops"));
        productCatalog.addCatalogProduct("macstudio", "Mac Studio",  Arrays.asList("desktops", "electronics"));
        productCatalog.addCatalogProduct("chromecast4k", "Chromecast 4K",  Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("boseqc45", "Bose QuietComfort 45",  Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("sonywh1000xm5", "Sony WH-1000XM5",  Arrays.asList("electronics"));
        productCatalog.addCatalogProduct("logitechmxkeys", "Logitech MX Keys",  Arrays.asList("electronics", "accessories"));
        productCatalog.addCatalogProduct("logitechmxmaster3s", "Logitech MX Master 3S", Arrays.asList("electronics", "accessories"));
        productCatalog.addCatalogProduct("canonr6", "Canon EOS R6", Arrays.asList("electronics", "photography"));
        productCatalog.addCatalogProduct("nikonz9", "Nikon Z9",  Arrays.asList("electronics", "photography"));
    }

    private void setupSampleStores() throws Exception {
        storeService.createStore(VALID_SESSION, "TechHeaven");
        storeService.createStore(VALID_SESSION, "GadgetWorld");
        storeService.createStore(VALID_SESSION, "GamingHub");

        String imageURL = "https://images.unsplash.com/photo-1624555130581-1d9cca783bc0?q=80&w=2071&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";
        storeService.addProductToStore(VALID_SESSION, "TechHeaven", "iphone13", "iPhone 13 128GB", "Latest iPhone", 999.99, 5, imageURL);
        storeService.addProductToStore(VALID_SESSION, "TechHeaven", "macbookair", "Macbook Air Used", "M2 Chip", 1299.99, 3, imageURL);
        storeService.addProductToStore(VALID_SESSION, "GadgetWorld", "pixel7", "Pixel 7 64GB 15MP Camera", "Android flagship", 599.99, 10, imageURL);
        storeService.addProductToStore(VALID_SESSION, "GadgetWorld", "galaxya54", "Galaxy A54", "Android flagship", 549.99, 10, imageURL);
        storeService.addProductToStore(VALID_SESSION, "GamingHub", "ps5", "PlayStation 5", "Next-gen console", 499.99, 8, imageURL);
        storeService.addProductToStore(VALID_SESSION, "GamingHub", "xboxseriesx", "Xbox Series X", "Gaming console", 499.99, 7, imageURL);
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