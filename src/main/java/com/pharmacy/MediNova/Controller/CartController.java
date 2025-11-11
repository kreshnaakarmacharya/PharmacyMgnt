package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.CartItem;
import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Model.Medicine;
import com.pharmacy.MediNova.Model.PrescriptionUpload;
import com.pharmacy.MediNova.Repository.PrescriptionUploadRepo;
import com.pharmacy.MediNova.Service.MedicineService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Controller
public class CartController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private PrescriptionUploadRepo prescriptionUploadRepo;

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
    public String getUploadPrescription(@RequestParam("medicineId") long medicineId, Model model) {
        model.addAttribute("medicineId", medicineId);
        return "Customer/UploadPrescription";
    }

    @GetMapping("/showCheckout")
    public String getCheckOut(){
        return "Customer/CheckOut";
    }

    @GetMapping("/proceedToCheckout")
    public String proceedToCheckout(HttpSession session, Model model) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cart");

        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/customer/viewCart";
        }

        boolean requiresPrescription = cartItems.stream()
                .anyMatch(item -> item.getMedicine().isRequiredPrescription());

        if (requiresPrescription) {
            // pick one medicine that needs a prescription
            long medId = cartItems.stream()
                    .filter(item -> item.getMedicine().isRequiredPrescription())
                    .findFirst()
                    .get()
                    .getMedicine()
                    .getId();

            model.addAttribute("message", "Some medicines in your cart require a doctor's prescription.");
            return "redirect:/showUploadPrescription?medicineId=" + medId;
        }

        return "redirect:/showCheckout";
    }

    @PostMapping("/uploadPrescription")
    public String uploadPrescription(
            @RequestParam("prescriptionFile") MultipartFile file,
            @RequestParam("medicineId") long medicineId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // ✅ Get logged-in customer ID from session
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        Long customerId = customer.getId();
        if (customerId == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to upload a prescription.");
            return "redirect:/login";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/showUploadPrescription?medicineId=" + medicineId;
        }

        try {
            // -------------------------------
            // 1️⃣ Define upload folder per customer
            // -------------------------------
            String baseDir = System.getProperty("user.dir") + "/uploads/prescriptions/customer_" + customerId + "/";
            File dir = new File(baseDir);
            if (!dir.exists()) dir.mkdirs();

            // 2️⃣ Create unique file name
            String originalFileName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFileName;
            File destination = new File(baseDir + fileName);

            // 3️⃣ Save file on disk
            file.transferTo(destination);

            // 4️⃣ Save record in DB
            PrescriptionUpload prescription = new PrescriptionUpload();
            prescription.setCustomerId(customerId);
            prescription.setMedicineId(medicineId);
            prescription.setImagePath("/uploads/prescriptions/customer_" + customerId + "/" + fileName); // Relative path for web
            prescription.setStatus(PrescriptionUpload.OrderStatus.PENDING);
            prescription.setUploadDate(LocalDateTime.now());
            prescriptionUploadRepo.save(prescription);

            // 5️⃣ Save path in session for preview
            session.setAttribute("uploadedPrescriptionPath", prescription.getImagePath());

            redirectAttributes.addFlashAttribute("success", "Prescription uploaded successfully!.");
            return "redirect:/showUploadPrescription?medicineId=" + medicineId;

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "File upload failed! " + e.getMessage());
            return "redirect:/showUploadPrescription?medicineId=" + medicineId;
        }
    }



}
