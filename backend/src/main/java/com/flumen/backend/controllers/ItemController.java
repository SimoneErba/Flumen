package com.flumen.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.flumen.backend.domain.Item;
import com.flumen.backend.models.UpdateModel;
import com.flumen.backend.models.input.ItemInput;
import com.flumen.backend.models.input.LocationInput;
import com.flumen.backend.services.ItemService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable String id) {
        Item item = itemService.getItemById(id);
        return item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build();
    }

    @PostMapping()
    public ResponseEntity<Item> createItem(@RequestBody ItemInput item) {
        
        Item newItem = itemService.createItem(item);
        return ResponseEntity.ok(newItem);
    }

    @PutMapping()
    public ResponseEntity<Item> updateItem(@RequestBody UpdateModel model) {
        Item updatedItem = itemService.updateItem(model);
        return updatedItem != null ? ResponseEntity.ok(updatedItem) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
