package edu.qut.inventory.service.repository;

import edu.qut.inventory.service.model.InventoryItemModel;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryItemRepository extends PagingAndSortingRepository<InventoryItemModel, UUID > {

}
