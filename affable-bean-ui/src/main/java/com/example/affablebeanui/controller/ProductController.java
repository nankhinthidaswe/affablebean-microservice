package com.example.affablebeanui.controller;

import com.example.affablebeanui.entity.Product;
import com.example.affablebeanui.model.CartItem;
import com.example.affablebeanui.service.CartService;
import com.example.affablebeanui.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ui")
public class ProductController {

    private final ProductService productService;
    private  final CartService cartService;

    @GetMapping("/transport")
    public String transport(){
        ResponseEntity responseEntity = productService.saveCartItem();
          if (responseEntity.getStatusCode().is2xxSuccessful()){
              return "redirect:/home";
          }
          return "redirect:/ui/checkoutView";
    }
    @QueryMapping
    public List<CartItem> cartItems(){
     return  cartService.getAllProducts().stream()
                .map(p -> new CartItem(p.getId(),p.getName(),p.getPrice(),p.getDescription(),p.getQuantity(),p.getLastUpdate().toLocalDate()))
                .collect(Collectors.toList());
    }

    @GetMapping("/transfer")
    public String checkoutTransfer(@ModelAttribute("total") double total, RedirectAttributes redirectAttributes){
        ResponseEntity responseEntity =productService.transfer("mary@gmail.com","john@gmail.com",total);
        if(responseEntity.getStatusCode().is2xxSuccessful()){
            redirectAttributes.addFlashAttribute("transfer",true);
            cartService.cartClear();
            return "redirect:/home";
        }
        redirectAttributes.addFlashAttribute("transfer-error", true);
        return "redirect:/ui/checkoutView";
    }
    @GetMapping("/products/{id}")
    @ResponseBody
    public List<Product> showAll(@PathVariable int id){
        return productService.findProductByCategory(id);
    }



  /*  @GetMapping("/products/")
    @ResponseBody
    public List<Product> showAll(){
        return productService.showAllProduct();
    }*/

    @GetMapping("/product/category")
    public  String showProduct(@RequestParam("id")int id, Model model){
        model.addAttribute("products",productService.findProductByCategory(id));

        return "products";
    }
    @GetMapping("/product/purchase")
    public String addCart(@RequestParam("id") int id){
        Product product = productService.purchaseProduct(id);
        return "redirect:/ui/product/category?id=" + product.getCategory().getId();
    }
    @GetMapping("/product/cartView")
    public String viewCart(Model model){
        model.addAttribute("cartItems",cartService.getAllProducts());
        model.addAttribute("product", new Product());
        return "cartView";
    }
    @PostMapping("/product/checkout")
    public String  checkOut(Product product){
        System.out.println("==============="+ product.getQuantitylist());
        int i = 0;
        for (Product cartItem:cartService.getAllProducts()){
            cartItem.setQuantity(product.getQuantitylist().get(i));
            i++;
        }
        cartService.getAllProducts().forEach(System.out::println);
        return "redirect:/ui/checkout-View";
    }
    @GetMapping("/checkout-View")
    public String toCheckoutView(Model model){
        model.addAttribute("transferError",model.containsAttribute("transfer-error"));
        return "checkoutView";
    }

    @GetMapping("/cart/clear")
    public String clearCart(){
        cartService.cartClear();
        return "redirect:/ui/home";
    }
    @GetMapping("/home")
    public String home(Model model, HttpServletRequest request){
        Boolean transfer = Boolean.valueOf(request.getParameter("transfer"));
        model.addAttribute("transfer","transfer");
        return "home";
    }

    @ModelAttribute("total")
    public double totalAmount(){
        return cartService.getAllProducts()
                .stream().map(p -> p.getQuantity() * p.getPrice())
                .mapToDouble(i ->i).sum();
    }
    @ModelAttribute("cartSize")
    public int cartSize(){
        return cartService.cartSize();
    }
}
