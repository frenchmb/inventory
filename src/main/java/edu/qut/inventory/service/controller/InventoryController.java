package edu.qut.inventory.service.controller;

import edu.qut.api.InventoryApi;
import edu.qut.inventory.service.model.InventoryItemModel;
import edu.qut.inventory.service.model.ManufacturerModel;
import edu.qut.inventory.service.repository.InventoryItemRepository;
import edu.qut.inventory.service.repository.OffsetPageable;
import edu.qut.model.InventoryItem;
import edu.qut.model.Manufacturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Validated
public class InventoryController implements InventoryApi {
    @Resource
    private InventoryItemRepository inventoryItemRepository;

    @Override
    public ResponseEntity<List<InventoryItem>> inventoryGet(@Min(0) @Valid Integer skip, @Min(0) @Max(50) @Valid Integer limit) {
        Pageable page = new OffsetPageable(limit, skip);
        Page<InventoryItemModel> results = inventoryItemRepository.findAll(page);

        if(results.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return toResponse(results);
    }

    @Override
    public ResponseEntity<InventoryItem> inventoryIdGet(UUID id) {
        Optional<InventoryItemModel> result = inventoryItemRepository.findById(id);
        return result.map(inventoryItemModel -> new ResponseEntity<>(toResponse(inventoryItemModel), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<Void> inventoryPost(@Valid InventoryItem inventoryItem) {
        Optional<InventoryItemModel> match = inventoryItemRepository.findById(inventoryItem.getId());
        if(match.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            inventoryItemRepository.save(toModel(inventoryItem));
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("Invalid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private InventoryItemModel toModel(InventoryItem inventoryItem) {
        InventoryItemModel inventory = new InventoryItemModel();

        UUID id = inventoryItem.getId();
        inventory.setId(id);
        inventory.setName(inventoryItem.getName());
        inventory.setReleaseDate(inventoryItem.getReleaseDate());
        ManufacturerModel manufacturer = new ManufacturerModel();
        inventory.setManufacturer(manufacturer);
        Manufacturer itemManufacturer = inventoryItem.getManufacturer();
        manufacturer.setHomePage(itemManufacturer.getHomePage());
        manufacturer.setName(itemManufacturer.getName());
        manufacturer.setPhone(itemManufacturer.getPhone());
        manufacturer.setId(id);

        return inventory;
    }

    private ResponseEntity<List<InventoryItem>> toResponse(Page<InventoryItemModel> results) {
        final ArrayList<InventoryItem> inventoryItems = new ArrayList<>();
        results.forEach(inventoryItemModel -> inventoryItems.add(toResponse(inventoryItemModel)));
        return new ResponseEntity<>(inventoryItems, HttpStatus.OK);
    }

    private InventoryItem toResponse(InventoryItemModel inventoryItemModel) {
        InventoryItem item = new InventoryItem();

        item.setId(inventoryItemModel.getId());
        item.setName(inventoryItemModel.getName());
        item.setReleaseDate(inventoryItemModel.getReleaseDate());

        Manufacturer manufacturer = new Manufacturer();
        item.setManufacturer(manufacturer);
        ManufacturerModel manufacturerModel = inventoryItemModel.getManufacturer();
        manufacturer.setHomePage(manufacturerModel.getHomePage());
        manufacturer.setName(manufacturerModel.getName());
        manufacturer.setPhone(manufacturerModel.getPhone());
        return item;
    }
}
