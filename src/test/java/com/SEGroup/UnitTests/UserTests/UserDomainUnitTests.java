package com.SEGroup.UnitTests.UserTests;

import com.SEGroup.Domain.User.*;
import com.SEGroup.Infrastructure.Repositories.InMemoryRepositories.UserRepository;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit-level tests for the concrete in-memory classes that live in
 * {@code com.SEGroup.Domain.User}.
 *
 * Naming follows the BDD style:
 * Given_<Preconditions>_When_<StateUnderTest>_Then_<ExpectedBehaviour>
 */
class UserDomainUnitTests {

    /* ───────────────────────────── Basket ───────────────────────────── */
    @Nested class BasketTests {
        Basket basket;
        
        @BeforeEach void newBasket() { basket = new Basket("S-X",null); }

        @Test void Given_EmptyBasket_When_AddItem_Then_SnapshotContainsQty1() {
            basket.add("P1", 1);
            assertEquals(1, basket.snapshot().get("P1"));
        }

        @Test void Given_ItemAdded_When_ChangeQty_Then_SnapshotUpdated() {
            basket.add("P1", 1);
            basket.change("P1", 5);
            assertEquals(5, basket.snapshot().get("P1"));
        }

        @Test void Given_ItemPresent_When_ChangeQtyToZero_Then_QtyStoredAsZero() {
            basket.add("P1", 2);
            basket.change("P1", 0);
            assertEquals(null, basket.snapshot().get("P1"));
        }
    }

    /* ────────────────────────── ShoppingCart ────────────────────────── */
    @Nested class ShoppingCartTests {
        ShoppingCart cart;

        @BeforeEach void newCart() { cart = new ShoppingCart("someId"); }

        @Test void Given_EmptyCart_When_AddItem_Then_StoreAndQtyCreated() {
            cart.add("S1", "P1", 3);
            assertEquals(3, cart.snapShot().get("S1").snapshot().get("P1"));
        }

        @Test void Given_ItemInCart_When_ChangeQty_Then_QtyUpdated() {
            cart.add("S1", "P1", 1);
            cart.changeQty("S1", "P1", 7);
            assertEquals(7, cart.snapShot().get("S1").snapshot().get("P1"));
        }

        @Test void Given_ItemInCart_When_ChangeQtyToZero_Then_QtyZeroStored() {
            cart.add("S1", "P1", 1);
            cart.changeQty("S1", "P1", 0);
            assertEquals(null, cart.snapShot().get("S1").snapshot().get("P1"));
        }

        @Test void Given_ItemsAcrossStores_When_Clear_Then_CartEmpty() {
            cart.add("S1", "P1", 1);
            cart.add("S2", "P2", 4);
            cart.clear();
            assertTrue(cart.snapShot().isEmpty());
        }
    }

    /* ───────────────────────────── User ─────────────────────────────── */
    @Nested class UserTests {
        User user;

        @BeforeEach void newUser() { user = new User("u@bgu.ac.il","zaziBazazi", "hash"); }

        @Test void Given_NewUser_When_AddOwnerRole_Then_HasRoleTrue() {
            user.addRole("Shop", Role.STORE_OWNER);
            assertTrue(user.hasRole("Shop", Role.STORE_OWNER));
        }

        @Test void Given_UserWithRole_When_RemoveRole_Then_HasRoleFalse() {
            user.addRole("Shop", Role.STORE_MANAGER);
            user.removeRole("Shop", Role.STORE_MANAGER);
            assertFalse(user.hasRole("Shop", Role.STORE_MANAGER));
        }

        @Test void Given_User_When_AddToCart_Then_ItemAppears() {
            user.addToCart("Shop", "P1");
            assertEquals(1, user.cart().snapShot()
                    .get("Shop").snapshot().get("P1"));
        }
    }

    /* ───────────────────────────── Guest ────────────────────────────── */
    @Nested class GuestTests {
        @Test void Given_NewGuest_When_GetId_And_Cart_Then_ValuesReturned() {
            ShoppingCart c = new ShoppingCart("g-123");
            Guest g = new Guest("g-123", Instant.now(), c);
            assertEquals("g-123", g.id());
            assertSame(c, g.cart());
        }
    }

    /* ─────────────────────── UserRepository (extras) ─────────────────── */
    @Nested class UserRepositoryExtras {
        UserRepository repo;

        @BeforeEach void freshRepo() { repo = new UserRepository(); }

        @Test void Given_UserWithCart_When_ClearCart_Then_CartEmpty() {
            repo.addUser("U", "u@mail", "h");
            repo.addToCart("u@mail", "1", "10"); // qty=1
            repo.clearUserCart("u@mail");
            assertTrue(repo.findUserByEmail("u@mail")
                    .cart().snapShot().isEmpty());
        }
    }
}
