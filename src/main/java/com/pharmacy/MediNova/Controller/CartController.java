package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Repository.ShippingDetailsRepo;
import com.pharmacy.MediNova.Service.MedicineService;
import com.pharmacy.MediNova.Service.PurchaseRecordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Controller
public class CartController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private PurchaseRecordService purchaseRecordService;

    @Autowired
    private ShippingDetailsRepo shippingDetailsRepo;

    @GetMapping("/customer/viewCart")
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        double grandTotal = 0;
        for (CartItem item : cart) {
            grandTotal += item.getTotalPrice();
        }

        model.addAttribute("cart", cart);
        model.addAttribute("grandTotal", grandTotal); // pass to Thymeleaf
        return "Customer/Cart";
    }



    @PostMapping("/addToCart")
    public String addToCart(@RequestParam("id") long medicineId, HttpSession session, RedirectAttributes redirectAttributes){
        Object user = session.getAttribute("loggedInCustomer");
        if(user == null){
            redirectAttributes.addFlashAttribute("message", "You are not logged in");
            return "redirect:/login";
        }
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        Medicine medicine = medicineService.findMedicineById(medicineId);
        boolean exists = false;

        for (CartItem item : cart) {
            if (Objects.equals(item.getMedicine().getId(), medicineId)) {
                item.setQuantity(item.getQuantity() + 1);
                exists = true;
                break;
            }
        }

        if (!exists) cart.add(new CartItem(medicine));

        session.setAttribute("cart", cart);
        return "redirect:/customer/viewCart";
    }
    @PostMapping("/customer/increaseQuantity/{id}")
    public String increaseQuantity(@PathVariable Long id, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            for (CartItem item : cart) {
                if (Objects.equals(item.getMedicine().getId(), id)) {
                    item.setQuantity(item.getQuantity() + 1);
                    break;
                }
            }
        }
        session.setAttribute("cart", cart);
        return "redirect:/customer/viewCart";
    }

    @PostMapping("/customer/decreaseQuantity/{id}")
    public String decreaseQuantity(@PathVariable Long id, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            Iterator<CartItem> iterator = cart.iterator();
            while (iterator.hasNext()) {
                CartItem item = iterator.next();
                if (Objects.equals(item.getMedicine().getId(), id)) {
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                    } else {
                        iterator.remove(); // remove if quantity = 0
                    }
                    break;
                }
            }
        }
        session.setAttribute("cart", cart);
        return "redirect:/customer/viewCart";
    }

    @GetMapping("/customer/removeFromCart/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart != null) {
            cart.removeIf(item -> Objects.equals(item.getMedicine().getId(), id));
        }
        session.setAttribute("cart", cart);
        return "redirect:/customer/viewCart";
    }
    @GetMapping("/customer/clearCart")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        // Remove the cart from session
        session.removeAttribute("cart");

        // Optional: send a message to display on the page
        redirectAttributes.addFlashAttribute("success", "Cart has been cleared successfully!");

        // Redirect back to the cart page
        return "redirect:/customer/viewCart";
    }

    @GetMapping("/showUploadPrescription")
    public String getUploadPrescription() {
        return "Customer/UploadPrescription";
    }

    @GetMapping("/showCheckout")
    public String getCheckOut(@AuthenticationPrincipal CustomCustomerDetails customerDetails, HttpSession session, Model model) {
        long customerId = customerDetails.getCustomerId();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        boolean requiresPrescription = cart.stream()
                .anyMatch(item -> item.getMedicine().isRequiredPrescription());

        if (requiresPrescription) {
            // Redirect to upload prescription page
            return "redirect:/showUploadPrescription";
        }

        double total = 0;
        for (CartItem item : cart) {
            total += item.getTotalPrice();
        }
        double deliveryCharge = 100;
        double grandTotal = total + deliveryCharge;

        List<ShippingDetails> addresses =
                shippingDetailsRepo.findByCustomerId(customerId);

        model.addAttribute("cartItems", cart);
        model.addAttribute("productTotal", total);
        model.addAttribute("deliveryCharge", deliveryCharge);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("addresses", addresses);
        return "Customer/CheckOut";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(@RequestParam("shippingAddressId") Long shippingAddressId, HttpSession session, RedirectAttributes redirectAttributes) {

        try {
            // Get cart items (from session or DB)
            List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cart");

            if (cartItems == null || cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "error", "Your cart is empty");
                return "redirect:/cart";
            }

            // Call service
            purchaseRecordService.savePurchase(cartItems,shippingAddressId);
            return "redirect:/customer/customerHomePage";

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());
            return "redirect:/showCheckout";
        }
    }
}
