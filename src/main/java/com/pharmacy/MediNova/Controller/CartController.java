package com.pharmacy.MediNova.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Repository.PurchaseRecordRepo;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class CartController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private PurchaseRecordService purchaseRecordService;

    @Autowired
    private ShippingDetailsRepo shippingDetailsRepo;
    @Autowired
    private PurchaseRecordRepo purchaseRecordRepo;

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

    @GetMapping("/showCheckout")
    public String getCheckOut(@AuthenticationPrincipal CustomCustomerDetails customerDetails, HttpSession session, Model model) {
        long customerId = customerDetails.getCustomerId();
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        boolean requiresPrescription = cart.stream()
                .anyMatch(item -> item.getMedicine().isRequiredPrescription());


        double total = 0;
        for (CartItem item : cart) {
            total += item.getTotalPrice();
        }
        double deliveryCharge = 1;
        double grandTotal = total + deliveryCharge;

        List<ShippingDetails> addresses =
                shippingDetailsRepo.findByCustomerId(customerId);

        model.addAttribute("cartItems", cart);
        model.addAttribute("productTotal", total);
        model.addAttribute("deliveryCharge", deliveryCharge);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("addresses", addresses);
        model.addAttribute("requiresPrescription", requiresPrescription);
        return "Customer/CheckOut";
    }

    @GetMapping("showUploadPrescription")
    public String showUploadPrescription(){
        return "Customer/UploadPrescription";
    }


    @PostMapping("/placeOrder")
    public String placeOrder(
            @RequestParam("shippingAddressId") Long shippingAddressId,
            HttpSession session,
            @RequestParam(value = "prescription", required = false) MultipartFile prescription,Model model
    ) {

        try {
            // Get cart items (from session or DB)
            List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cart");

            if (cartItems == null || cartItems.isEmpty()) {
                return "redirect:/cart";
            }
            // Call service
           PurchaseRecord record= purchaseRecordService.savePurchase(cartItems,shippingAddressId, prescription);
            if(record.isRequiredPrescription()){
                return "redirect:/waitingPage";
            }
            else{
                long orderId=record.getId();
                return "redirect:/signature?orderId="+orderId;
            }


        } catch (Exception ex) {
            return "redirect:/showCheckout";
        }
    }
    @GetMapping("/waitingPage")
    public String waitingPage(){
        return "Customer/WaitingPage";
    }

    @GetMapping("/orderSuccess")
    public String orderSuccess(){
        return "Customer/OrderSuccess";
    }

    @GetMapping("/paymentFailed")
    public String paymentFailed(){
        return "Customer/PaymentFailed";
    }

    @PostMapping("/esewa/success")
    public String esewaSuccess(@RequestParam("data") String data) throws Exception{
        String decodedJson=new String(Base64.getDecoder().decode(data));
        System.out.println(decodedJson);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node=mapper.readTree(decodedJson);

        String status=node.get("status").asText();
        String transactionUuid=node.get("transaction_uuid").asText();
        String transactionCode=node.get("transaction_code").asText();
        Double total_amount=node.get("total_amount").asDouble();

        if("SUCCESSFULL".equalsIgnoreCase(status)){
            PurchaseRecord record=purchaseRecordRepo.findByTransactionUuid(transactionUuid).
                    orElseThrow(()->new RuntimeException("Order not found"));
            record.setPayment(PurchaseRecord.Payment.SUCCESSFULL);
            record.setEsewaRefId(transactionCode);
            record.setTransactionUuid(transactionUuid);
            purchaseRecordRepo.save(record);
        }
        return "redirect:/orderSuccess";
    }

    @PostMapping("/esewa/failure")
    public String esewaFail(@RequestParam(value="data" ,required = false) String data){

        String decodedJson = new String(Base64.getDecoder().decode(data));
        System.out.println(decodedJson);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(decodedJson);

            String transactionUuid = node.get("transaction_uuid").asText();

            PurchaseRecord record = purchaseRecordRepo
                    .findByTransactionUuid(transactionUuid)
                    .orElse(null);

            if (record != null) {
                record.setPayment(PurchaseRecord.Payment.UNSUCCESSFULL);
                purchaseRecordRepo.save(record);
            }
        } catch (Exception ignored) {}
        return "redirect:/paymentFailed";
    }
}
