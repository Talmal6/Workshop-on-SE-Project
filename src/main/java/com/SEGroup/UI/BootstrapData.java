package com.SEGroup.UI;

import com.SEGroup.Domain.*;
import com.SEGroup.Infrastructure.ExternalPaymentService;
import com.SEGroup.Infrastructure.PasswordEncoder;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.*;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.GuestRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.InMemoryProductCatalog;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.StoreRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.TransactionRepository;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.UserRepository;
import com.SEGroup.Domain.IShippingService;
import com.SEGroup.Service.MockShippingService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
class BootstrapData {
        //a

    private String getProductImage(String keyword) {
        String key = (keyword == null) ? "" : keyword.toLowerCase();

        /* ---------- ELECTRONICS ---------- */
        if (key.contains("phone"))
            return "https://images.unsplash.com/photo-1512499617640-c2f999098c63?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("laptop"))
            return "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("earbud") || key.contains("audio") || key.contains("headphone"))
            return "https://images.unsplash.com/photo-1585386959984-a41552231617?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("watch"))
            return "https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("monitor"))
            return "https://images.unsplash.com/photo-1587825140708-030e382b97c8?auto=format&fit=crop&w=400&h=400&q=80";

        /* ---------- FASHION -------------- */
        if (key.contains("jacket"))
            return "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("shirt") || key.contains("tee") || key.contains("graphic"))
            return "https://images.unsplash.com/photo-1523289333742-bea4fa21c8f8?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("shoe"))
            return "https://images.unsplash.com/photo-1511974035430-5de47d3b95da?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("bag") || key.contains("tote"))
            return "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("scarf"))
            return "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=400&h=400&q=80";

        /* ---------- HOME ----------------- */
        if (key.contains("coffee"))
            return "https://images.unsplash.com/photo-1511920170033-f8396924c348?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("vacuum") || key.contains("robot"))
            return "https://images.unsplash.com/photo-1615540122321-9b9371bd3432?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("bedding") || key.contains("sheet"))
            return "https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("knife"))
            return "https://images.unsplash.com/photo-1526040652367-ac003a0475fe?auto=format&fit=crop&w=400&h=400&q=80";

        if (key.contains("smart") && key.contains("home"))
            return "https://images.unsplash.com/photo-1518444024084-31c5455b03d7?auto=format&fit=crop&w=400&h=400&q=80";

        /* ---------- DEFAULT -------------- */
        return "https://images.unsplash.com/photo-1585386959984-a41552231617?auto=format&fit=crop&w=400&h=400&q=80";
    }
    @PostConstruct
    void initDemoData() {
        PasswordEncoder encoder = new PasswordEncoder();
        UserRepository users = new UserRepository();
        StoreRepository stores = new StoreRepository();
        InMemoryProductCatalog catalog = new InMemoryProductCatalog();

        /* USERS ----------------------------------------------- */
        System.out.println("Initializing users...");
        // Admin
        users.addUser("System Admin", "admin@demo.com", encoder.encrypt("admin123"));


        // Store owners
        users.addUser("Demo Owner", "owner@demo.com", encoder.encrypt("demo123"));
        users.addUser("Co-owner", "co-owner@demo.com", encoder.encrypt("demo123"));
        users.addUser("Tech Store Owner", "tech@demo.com", encoder.encrypt("demo123"));
        users.addUser("Fashion Store Owner", "fashion@demo.com", encoder.encrypt("demo123"));
        users.addUser("Home Goods Owner", "home@demo.com", encoder.encrypt("demo123"));

        // Regular users
        users.addUser("Regular User", "user@demo.com", encoder.encrypt("demo123"));
        users.addUser("Shopper One", "shopper1@demo.com", encoder.encrypt("demo123"));
        users.addUser("Shopper Two", "shopper2@demo.com", encoder.encrypt("demo123"));
        users.addUser("Tech Enthusiast", "tech.fan@demo.com", encoder.encrypt("demo123"));
        users.addUser("Fashion Lover", "fashionista@demo.com", encoder.encrypt("demo123"));

        /* STORES ---------------------------------------------- */
        System.out.println("Creating stores...");
        // Demo Store
        String demoStore = "Demo Store";
        stores.createStore(demoStore, "owner@demo.com");
        stores.appointOwner(demoStore, "owner@demo.com", "co-owner@demo.com",false);
        stores.updateStoreDescription(demoStore, "owner@demo.com",
                "Your one-stop shop for all premium electronics, fashion, and home goods. We offer the best prices and quality service.");

        // Tech Store
        String techStore = "Tech Gadgets";
        stores.createStore(techStore, "tech@demo.com");
        stores.appointManager(techStore, "tech@demo.com", "user@demo.com",
                List.of("VIEW_ONLY", "MANAGE_PRODUCTS"),false);
        stores.updateStoreDescription(techStore, "tech@demo.com",
                "Cutting-edge technology at affordable prices. From smartphones to laptops, we have everything a tech enthusiast needs.");

        // Fashion Store
        String fashionStore = "Fashion Hub";
        stores.createStore(fashionStore, "fashion@demo.com");
        stores.updateStoreDescription(fashionStore, "fashion@demo.com",
                "Trendy fashion items from top designers. Stay stylish with our curated collection of clothing and accessories.");

        // Home Goods Store
        String homeStore = "Home Essentials";
        stores.createStore(homeStore, "home@demo.com");
        stores.updateStoreDescription(homeStore, "home@demo.com",
                "Everything you need to make your house a home. Quality kitchen appliances, bedding, and home decor at great prices.");

        /* CATALOG PRODUCTS ------------------------------------ */
        System.out.println("Adding catalog products...");

        // Technology products - simplified catalog product with just ID, name, and categories
        catalog.addCatalogProduct("TECH-001", "Smartphone X","IPHONE","Flagship smartphone with 6.1\" OLED display, 12 MP camera system, 5G connectivity, and all-day battery life.",
                List.of("Electronics", "Phones"));
        catalog.addCatalogProduct("TECH-002", "Laptop Pro", "DELL","",
                List.of("Electronics", "Computers"));
        catalog.addCatalogProduct("TECH-003", "Wireless Earbuds","Acme Audio","",
                List.of("Electronics", "Audio"));
        catalog.addCatalogProduct("TECH-004", "Smart Watch Elite","","",
                List.of("Electronics", "Wearables"));
        catalog.addCatalogProduct("TECH-005", "4K Gaming Monitor","","",
                List.of("Electronics", "Displays"));

        // Fashion products - simplified catalog product with just ID, name, and categories
        catalog.addCatalogProduct("FASH-001", "Designer T-Shirt","","",
                List.of("Clothing", "T-Shirts"));
        catalog.addCatalogProduct("FASH-002", "Leather Jacket","","",
                List.of("Clothing", "Outerwear"));
        catalog.addCatalogProduct("FASH-003", "Running Shoes","","",
                List.of("Footwear", "Sports"));
        catalog.addCatalogProduct("FASH-004", "Designer Handbag","","",
                List.of("Accessories", "Bags"));
        catalog.addCatalogProduct("FASH-005", "Silk Scarf","","",
                List.of("Accessories", "Scarves"));

        // Home products - simplified catalog product with just ID, name, and categories
        catalog.addCatalogProduct("HOME-001", "Coffee Maker Deluxe","","",
                List.of("Home", "Kitchen"));
        catalog.addCatalogProduct("HOME-002", "Smart Vacuum Robot","","",
                List.of("Home", "Cleaning"));
        catalog.addCatalogProduct("HOME-003", "Luxury Bedding Set","","",
                List.of("Home", "Bedroom"));
        catalog.addCatalogProduct("HOME-004", "Chef's Knife Set","","",
                List.of("Home", "Kitchen"));
        catalog.addCatalogProduct("HOME-005", "Smart Home Hub","","",
                List.of("Home", "Smart Home"));

        /* STORE PRODUCTS ------------------------------------- */
        System.out.println("Adding store products...");

        // Demo Store products - with descriptions and real image URLs
        System.out.println("Adding Demo Store products...");
        String p1 = stores.addProductToStore("owner@demo.com", demoStore, "TECH-001",
                "Smartphone X Pro", "Flagship smartphone with 6.7-inch AMOLED display, 108MP camera system, 5G connectivity, and all-day battery life. Includes 1-year warranty and premium accessories.", 999.99, 10, true, getProductImage("Phone"), List.of("Electronics", "Phones"));
        // 🔨 start a quick demo auction on that first product:
        // ownerEmail, storeName, productId, startingPrice, durationMillis
        stores.startAuction("owner@demo.com",
                demoStore,       // ← use the variable, not the string literal
                p1,
                100.0,
                new Date(System.currentTimeMillis() + 5 * 60_000L)
        );
        String p2 = stores.addProductToStore("owner@demo.com", demoStore, "TECH-002",
                "Laptop Pro Max", "High-performance laptop featuring the latest processor, 32GB RAM, 1TB SSD, and dedicated graphics. Perfect for professionals, designers, and gamers.", 1499.99, 5, true, getProductImage("Laptop"),List.of("Electronics", "Computers"));
        String p3 = stores.addProductToStore("owner@demo.com", demoStore, "HOME-001",
                "Coffee Maker Deluxe", "Fully automatic coffee maker with built-in grinder, programmable timer, and thermal carafe. Enjoy barista-quality coffee at home.", 129.99, 20, true, getProductImage("Coffee"),List.of("Home", "Kitchen"));
        String p4 = stores.addProductToStore("owner@demo.com", demoStore, "FASH-002",
                "Premium Leather Jacket", "Handcrafted genuine leather jacket with quilted lining and premium hardware. Classic style that never goes out of fashion.", 349.99, 8, true, getProductImage("Jacket"),List.of("Clothing", "Outerwear"));

        // Add store products to catalog with images and initial ratings
        System.out.println("Adding Demo Store product images...");
        catalog.addStoreProductEntryWithImage("TECH-001", demoStore, p1, 999.99, 10, 4.8, "Smartphone X Pro",
                getProductImage("Phone"));
        catalog.addStoreProductEntryWithImage("TECH-002", demoStore, p2, 1499.99, 5, 4.6, "Laptop Pro Max",
                getProductImage("Laptop"));
        catalog.addStoreProductEntryWithImage("HOME-001", demoStore, p3, 129.99, 20, 4.5, "Coffee Maker Deluxe",
                getProductImage("Coffee"));
        catalog.addStoreProductEntryWithImage("FASH-002", demoStore, p4, 349.99, 8, 4.7, "Premium Leather Jacket",
                getProductImage("Jacket"));

        // Tech Store products - with descriptions and real image URLs
        System.out.println("Adding Tech Store products...");
        String p5 = stores.addProductToStore("tech@demo.com", techStore, "TECH-001",
                "Smartphone X Limited Edition", "Exclusive version with extended warranty, premium case, and screen protector. Features enhanced storage and special color options.", 1049.99, 15, true, getProductImage("Phone"),List.of("Electronics", "Phones"));
        String p6 = stores.addProductToStore("tech@demo.com", techStore, "TECH-002",
                "Laptop Pro Developer Edition", "Specially configured for developers with Linux pre-installed, extended battery, and free carrying case.", 1649.99, 8, true, getProductImage("Laptop"),List.of("Electronics", "Computers"));
        String p7 = stores.addProductToStore("tech@demo.com", techStore, "TECH-003",
                "Wireless Earbuds Pro", "Premium wireless earbuds with active noise cancellation, transparency mode, and wireless charging case.", 179.99, 30, true, getProductImage("Audio"),List.of("Electronics", "Audio"));
        String p8 = stores.addProductToStore("tech@demo.com", techStore, "TECH-004",
                "Smart Watch Elite GPS", "Advanced smartwatch with cellular connectivity, ECG monitoring, and premium band options.", 349.99, 12, true, getProductImage("Watch"),List.of("Electronics", "Wearables"));
        String p9 = stores.addProductToStore("tech@demo.com", techStore, "TECH-005",
                "Ultra Gaming Monitor", "34-inch ultra-wide curved gaming monitor with 165Hz refresh rate and G-Sync support.", 699.99, 7, true, getProductImage("Monitor"),List.of("Electronics", "Displays"));

        // Add Tech Store products to catalog with images and ratings
        System.out.println("Adding Tech Store product images...");
        catalog.addStoreProductEntryWithImage("TECH-001", techStore, p5, 1049.99, 15, 4.9, "Smartphone X Limited Edition",
                getProductImage("Phone"));
        catalog.addStoreProductEntryWithImage("TECH-002", techStore, p6, 1649.99, 8, 4.7, "Laptop Pro Developer Edition",
                getProductImage("Laptop"));
        catalog.addStoreProductEntryWithImage("TECH-003", techStore, p7, 179.99, 30, 4.6, "Wireless Earbuds Pro",
                getProductImage("Audio"));
        catalog.addStoreProductEntryWithImage("TECH-004", techStore, p8, 349.99, 12, 4.8, "Smart Watch Elite GPS",
                getProductImage("Watch"));
        catalog.addStoreProductEntryWithImage("TECH-005", techStore, p9, 699.99, 7, 4.9, "Ultra Gaming Monitor",
                getProductImage("Monitor"));

        // Fashion Store products - with descriptions and real image URLs
        System.out.println("Adding Fashion Store products...");
        String p10 = stores.addProductToStore("fashion@demo.com", fashionStore, "FASH-001",
                "Designer Graphic Tee", "Limited edition designer t-shirt with exclusive print. Made from organic cotton for maximum comfort and style.", 59.99, 100, true, getProductImage("Clothing"), List.of("Clothing", "T-Shirts"));
        String p11 = stores.addProductToStore("fashion@demo.com", fashionStore, "FASH-002",
                "Vintage Leather Jacket", "Classic leather jacket with distressed finish and premium hardware. Timeless style that gets better with age.", 329.99, 15, true, getProductImage("Jacket"), List.of("Clothing", "Outerwear"));
        String p12 = stores.addProductToStore("fashion@demo.com", fashionStore, "FASH-003",
                "Performance Running Shoes", "Engineered for maximum comfort and performance with responsive cushioning and breathable materials.", 149.99, 50, true, getProductImage("Shoe"),List.of("Footwear", "Sports"));
        String p13 = stores.addProductToStore("fashion@demo.com", fashionStore, "FASH-004",
                "Signature Tote Bag", "Spacious designer tote with premium leather trim and durable canvas construction. Perfect for work or weekend.", 199.99, 25, true, getProductImage("Bag"),List.of("Accessories", "Bags"));
        String p14 = stores.addProductToStore("fashion@demo.com", fashionStore, "FASH-005",
                "Designer Silk Scarf", "Luxurious silk scarf with hand-painted design. Versatile accessory to elevate any outfit.", 89.99, 40, true, getProductImage("Scarf"),List.of("Accessories", "Scarves"));

        // Add Fashion Store products to catalog with images and ratings
        System.out.println("Adding Fashion Store product images...");
        catalog.addStoreProductEntryWithImage("FASH-001", fashionStore, p10, 59.99, 100, 4.5, "Designer Graphic Tee",
                getProductImage("Clothing"));
        catalog.addStoreProductEntryWithImage("FASH-002", fashionStore, p11, 329.99, 15, 4.8, "Vintage Leather Jacket",
                getProductImage("Jacket"));
        catalog.addStoreProductEntryWithImage("FASH-003", fashionStore, p12, 149.99, 50, 4.6, "Performance Running Shoes",
                getProductImage("Shoe"));
        catalog.addStoreProductEntryWithImage("FASH-004", fashionStore, p13, 199.99, 25, 4.7, "Signature Tote Bag",
                getProductImage("Bag"));
        catalog.addStoreProductEntryWithImage("FASH-005", fashionStore, p14, 89.99, 40, 4.4, "Designer Silk Scarf",
                getProductImage("Scarf"));

        // Home Store products - with descriptions and real image URLs
        System.out.println("Adding Home Store products...");
        String p15 = stores.addProductToStore("home@demo.com", homeStore, "HOME-001",
                "Barista Coffee Maker", "Professional-grade coffee maker with built-in grinder, pressure control, and milk frother. Make cafe-quality coffee at home.", 199.99, 18, true, getProductImage("Coffee"),List.of("Home", "Kitchen"));
        String p16 = stores.addProductToStore("home@demo.com", homeStore, "HOME-002",
                "Smart Robot Vacuum", "AI-powered vacuum with mapping technology, scheduling, and app control. Keep your home clean with minimal effort.", 299.99, 10, true, getProductImage("Vacuum"),List.of("Home", "Cleaning"));
        String p17 = stores.addProductToStore("home@demo.com", homeStore, "HOME-003",
                "Premium Egyptian Cotton Bedding", "Luxurious 1000 thread count Egyptian cotton sheets, duvet cover, and pillowcases for the ultimate sleep experience.", 249.99, 25, true, getProductImage("Bedding"),List.of("Home", "Bedroom"));
        String p18 = stores.addProductToStore("home@demo.com", homeStore, "HOME-004",
                "Professional Knife Collection", "Hand-forged chef's knives with premium steel blades and ergonomic handles. Essential tools for any serious cook.", 179.99, 15, true, getProductImage("Knife"),List.of("Home", "Kitchen"));
        String p19 = stores.addProductToStore("home@demo.com", homeStore, "HOME-005",
                "Smart Home Control Center", "Central hub for all your smart home devices with voice control, automation, and security features.", 159.99, 20, true, getProductImage("Smart Home"),List.of("Home", "Smart Home"));

        // Add Home Store products to catalog with images and ratings
        System.out.println("Adding Home Store product images...");
        catalog.addStoreProductEntryWithImage("HOME-001", homeStore, p15, 199.99, 18, 4.7, "Barista Coffee Maker",
                getProductImage("Coffee"));
        catalog.addStoreProductEntryWithImage("HOME-002", homeStore, p16, 299.99, 10, 4.5, "Smart Robot Vacuum",
                getProductImage("Vacuum"));
        catalog.addStoreProductEntryWithImage("HOME-003", homeStore, p17, 249.99, 25, 4.9, "Premium Egyptian Cotton Bedding",
                getProductImage("Bedding"));
        catalog.addStoreProductEntryWithImage("HOME-004", homeStore, p18, 179.99, 15, 4.8, "Professional Knife Collection",
                getProductImage("Knife"));
        catalog.addStoreProductEntryWithImage("HOME-005", homeStore, p19, 159.99, 20, 4.6, "Smart Home Control Center",
                getProductImage("Smart Home"));

        /* ADD RATINGS & REVIEWS ------------------------------ */
        System.out.println("Adding ratings and reviews...");
        // Demo Store ratings
        stores.rateStore("user@demo.com", demoStore, 5, "Excellent selection and fast shipping!");
        stores.rateStore("shopper1@demo.com", demoStore, 4, "Good prices and quality products.");
        stores.rateStore("shopper2@demo.com", demoStore, 5, "Great customer service and product range.");
        stores.rateStore("tech.fan@demo.com", demoStore, 4, "Wide variety of tech products at good prices.");

        // Tech Store ratings
        stores.rateStore("user@demo.com", techStore, 5, "Best tech store with cutting-edge products!");
        stores.rateStore("shopper1@demo.com", techStore, 5, "Amazing selection of gadgets and great service.");
        stores.rateStore("shopper2@demo.com", techStore, 4, "Good prices on high-end technology.");

        // Fashion Store ratings
        stores.rateStore("user@demo.com", fashionStore, 4, "Trendy fashion items at reasonable prices.");
        stores.rateStore("fashionista@demo.com", fashionStore, 5, "Love the designer collections!");
        stores.rateStore("shopper1@demo.com", fashionStore, 5, "High-quality fashion items and fast delivery.");

        // Home Store ratings
        stores.rateStore("user@demo.com", homeStore, 5, "Great selection of home goods!");
        stores.rateStore("shopper2@demo.com", homeStore, 4, "Quality kitchen products at good prices.");

        // Product ratings and reviews
        System.out.println("Adding product ratings...");
        // Demo Store product ratings
        stores.rateProduct("user@demo.com", demoStore, p1, 5, "Best smartphone I've ever owned! Amazing camera and battery life.");
        stores.rateProduct("shopper1@demo.com", demoStore, p1, 4, "Great phone with excellent features. A bit pricey though.");
        stores.rateProduct("tech.fan@demo.com", demoStore, p1, 5, "Incredible performance and the camera is unbeatable!");
        stores.rateProduct("user@demo.com", demoStore, p2, 5, "Perfect for my design work. Fast and powerful!");
        stores.rateProduct("tech.fan@demo.com", demoStore, p2, 4, "Great performance for development and gaming.");
        stores.rateProduct("shopper1@demo.com", demoStore, p3, 5, "Makes delicious coffee, very easy to use!");
        stores.rateProduct("shopper2@demo.com", demoStore, p4, 4, "Quality leather and comfortable fit. Love it!");

        // Tech Store product ratings
        stores.rateProduct("user@demo.com", techStore, p5, 5, "The limited edition is worth every penny!");
        stores.rateProduct("shopper1@demo.com", techStore, p5, 5, "Amazing exclusive features and the color is gorgeous.");
        stores.rateProduct("tech.fan@demo.com", techStore, p7, 5, "These earbuds have incredible sound quality!");
        stores.rateProduct("shopper2@demo.com", techStore, p8, 4, "Love all the health features on this smartwatch.");
        stores.rateProduct("shopper1@demo.com", techStore, p9, 5, "Incredible gaming experience on this monitor!");

        // Fashion Store product ratings
        stores.rateProduct("fashionista@demo.com", fashionStore, p10, 5, "Super stylish and comfortable fabric!");
        stores.rateProduct("shopper1@demo.com", fashionStore, p11, 4, "Beautiful leather jacket that fits perfectly.");
        stores.rateProduct("user@demo.com", fashionStore, p13, 5, "This tote is spacious and looks so elegant!");
        stores.rateProduct("fashionista@demo.com", fashionStore, p12, 4, "Great running shoes, very comfortable!");
        stores.rateProduct("shopper2@demo.com", fashionStore, p14, 5, "Gorgeous scarf, gets many compliments!");

        // Home Store product ratings
        stores.rateProduct("shopper2@demo.com", homeStore, p15, 5, "Makes the best coffee I've ever had at home!");
        stores.rateProduct("user@demo.com", homeStore, p16, 4, "The vacuum works great even with pets in the house.");
        stores.rateProduct("shopper1@demo.com", homeStore, p17, 5, "Softest sheets ever! Worth every penny.");
        stores.rateProduct("user@demo.com", homeStore, p18, 4, "Professional quality knives, very sharp!");
        stores.rateProduct("shopper2@demo.com", homeStore, p19, 5, "Great control hub for all my smart devices!");

        /* CATALOG PRODUCTS WITH EXPANDED CATEGORIES ------------ */
        System.out.println("Adding enhanced catalog products with rich categorization...");

        // Technology products with expanded categories - simplified catalog product structure
        catalog.addCatalogProduct("TECH-006", "Noise-Cancelling Headphones","","",
                List.of("Electronics", "Audio", "Headphones", "Over-Ear", "Noise Cancelling", "Wireless", "Premium", "Studio Quality"));

        catalog.addCatalogProduct("TECH-007", "Professional DSLR Camera","","",
                List.of("Electronics", "Cameras", "DSLR", "Professional", "Full-Frame", "4K Video", "High Resolution", "Photography"));

        catalog.addCatalogProduct("TECH-008", "Gaming Console Pro","","",
                List.of("Electronics", "Gaming", "Consoles", "Next-Gen", "Ray Tracing", "8K", "Entertainment", "High Performance"));

        catalog.addCatalogProduct("FASH-006", "Designer Sunglasses","","",
                List.of("Accessories", "Eyewear", "Sunglasses", "Designer", "Polarized", "UV Protection", "Fashion", "Summer"));

        catalog.addCatalogProduct("FASH-007", "Waterproof Hiking Boots","","",
                List.of("Footwear", "Outdoor", "Hiking", "Waterproof", "Durable", "Adventure", "All-Terrain", "Rugged"));

        catalog.addCatalogProduct("HOME-006", "Air Purifier Premium","","",
                List.of("Home", "Appliances", "Air Purifiers", "HEPA", "UV Sterilization", "Health", "Smart", "Quiet"));

        catalog.addCatalogProduct("HOME-007", "Smart Refrigerator","","",
                List.of("Home", "Kitchen", "Appliances", "Refrigerators", "Smart", "Energy Efficient", "Connected", "Premium"));

        catalog.addCatalogProduct("SPORT-001", "Carbon Fiber Road Bike","","",
                List.of("Sports", "Cycling", "Bikes", "Road Bikes", "Carbon Fiber", "Professional", "Racing", "Lightweight"));

        catalog.addCatalogProduct("SPORT-002", "Smart Fitness Tracker","","",
                List.of("Electronics", "Wearables", "Fitness", "Health", "Heart Rate Monitor", "GPS", "Tracking", "Coaching"));

        catalog.addCatalogProduct("BEAUTY-001", "Premium Skincare Set","","",
                List.of("Beauty", "Skincare", "Anti-Aging", "Premium", "Facial Care", "Moisturizers", "Serums", "Sets"));

        /* Initialize services -------------------------------- */
        System.out.println("Initializing services...");

        // Create a mock shipping service
        MockShippingService shippingService = new MockShippingService();

        ServiceLocator.initialize(
                new GuestRepository(),            // guests
                users,                            // users
                new TransactionRepository(),      // transactions
                stores,                           // stores
                catalog,                          // catalog
                new ExternalPaymentService()         // payment
//                shippingService                   // shipping service
        );

        System.out.println("Bootstrap data initialization complete!");
    }
}