package com.livedatatrail.backend.controllers;

import com.livedatatrail.backend.models.Item;
import com.livedatatrail.backend.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestParam String name, @RequestBody Map<String, Object> properties) {
        Item newItem = itemService.createItem(name, properties);
        return ResponseEntity.ok(newItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable String id, @RequestParam String name, @RequestBody Map<String, Object> properties) {
        Item updatedItem = itemService.updateItem(id, name, properties);
        return updatedItem != null ? ResponseEntity.ok(updatedItem) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
