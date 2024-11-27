import java.util.*;

public class Main
{
    private static final int DEFAULT_RESTOCK_THRESHOLD = 10;

    // Data structure to store inventory
    private final Map<String, Item> inventoryMap; // For unique item tracking by ID
    private final Map<String, PriorityQueue<Item>> categoryMap; // For category-wise sorting

    public Main() {
        inventoryMap = new HashMap<>();
        categoryMap = new TreeMap<>();
    }

    // Add or update an item in the inventory
    public void addOrUpdateItem(String id, String name, String category, int quantity) {
        // Check for invalid input
        if (id == null || id.isEmpty()) {
            System.out.println("Error: Item ID cannot be null or empty.");
            return;
        }
        if (name == null || name.isEmpty()) {
            System.out.println("Error: Item name cannot be null or empty.");
            return;
        }
        if (category == null || category.isEmpty()) {
            System.out.println("Error: Category cannot be null or empty.");
            return;
        }
        if (quantity < 0) {
            System.out.println("Error: Quantity cannot be negative.");
            return;
        }

        Item newItem = new Item(id, name, category, quantity);

        // Update or add the item
        if (inventoryMap.containsKey(id)) {
            Item existingItem = inventoryMap.get(id);
            removeFromCategory(existingItem); // Remove from category before updating

            existingItem.setName(name);
            existingItem.setCategory(category);
            existingItem.setQuantity(quantity);
            addToCategory(existingItem);

            System.out.println("Item successfully updated: " + existingItem);

            // Restock notification
            if (quantity < DEFAULT_RESTOCK_THRESHOLD) {
                System.out.println("Warning: Item \"" + name + "\" is low in stock after update. Quantity: " + quantity + ". Restock soon.");
            }
        } else {
            inventoryMap.put(id, newItem);
            addToCategory(newItem);
            System.out.println("Item successfully added: " + newItem);

            // Restock notification
            if (quantity < DEFAULT_RESTOCK_THRESHOLD) {
                System.out.println("Warning: Item \"" + name + "\" is low in stock. Quantity: " + quantity + ". Consider restocking soon.");
            }
        }
    }

    // Remove an item by ID
    public void removeItem(String id) {
        if (id == null || id.isEmpty()) {
            System.out.println("Error: Item ID cannot be null or empty.");
            return;
        }

        if (inventoryMap.containsKey(id)) {
            Item item = inventoryMap.remove(id);
            removeFromCategory(item);
            System.out.println("Item successfully removed: " + item);
        } else {
            System.out.println("Error: Item with ID '" + id + "' not found. Cannot remove it.");
        }
    }

    // Get all items in a category
    public List<Item> getItemsByCategory(String category) {
        if (category == null || category.isEmpty()) {
            System.out.println("Error: Category cannot be null or empty.");
            return Collections.emptyList();
        }

        PriorityQueue<Item> items = categoryMap.getOrDefault(category, new PriorityQueue<>());
        if (items.isEmpty()) {
            System.out.println("No items found in the category: '" + category + "'. Please check the category or try adding items.");
            return Collections.emptyList();
        }

        System.out.println("Items in category '" + category + "':");
        return new ArrayList<>(items);
    }

    // Get the top k items by quantity
    public List<Item> getTopKItems(int k) {
        if (k <= 0) {
            System.out.println("Error: 'k' must be a positive integer. Please provide a valid number.");
            return Collections.emptyList();
        }

        PriorityQueue<Item> maxHeap = new PriorityQueue<>((a, b) -> b.getQuantity() - a.getQuantity());
        maxHeap.addAll(inventoryMap.values());

        List<Item> topKItems = new ArrayList<>();
        while (k-- > 0 && !maxHeap.isEmpty()) {
            topKItems.add(maxHeap.poll());
        }

        if (topKItems.isEmpty()) {
            System.out.println("Error: No items available to show the top " + k + " items. Inventory might be empty.");
            return Collections.emptyList();
        }

        System.out.println("Top " + k + " items with the highest quantity:");
        return topKItems;
    }

    // Merge another inventory into this one
    public void mergeInventory(Main other) {
        if (other == null) {
            System.out.println("Error: Cannot merge with a null inventory.");
            return;
        }

        System.out.println("Merging inventory from another warehouse...");
        for (Item otherItem : other.inventoryMap.values()) {
            if (inventoryMap.containsKey(otherItem.getId())) {
                Item existingItem = inventoryMap.get(otherItem.getId());
                if (otherItem.getQuantity() > existingItem.getQuantity()) {
                    removeFromCategory(existingItem);
                    existingItem.setQuantity(otherItem.getQuantity());
                    addToCategory(existingItem);
                    System.out.println("Updated item (higher quantity): " + existingItem);
                }
            } else {
                addOrUpdateItem(otherItem.getId(), otherItem.getName(), otherItem.getCategory(), otherItem.getQuantity());
                System.out.println("Added new item: " + otherItem);
            }
        }
    }

    // Helper to add item to category map
    private void addToCategory(Item item) {
        categoryMap.putIfAbsent(item.getCategory(), new PriorityQueue<>((a, b) -> b.getQuantity() - a.getQuantity()));
        categoryMap.get(item.getCategory()).add(item);
    }

    // Helper to remove item from category map
    private void removeFromCategory(Item item) {
        PriorityQueue<Item> items = categoryMap.get(item.getCategory());
        if (items != null) {
            items.remove(item);
            if (items.isEmpty()) {
                categoryMap.remove(item.getCategory());
            }
        }
    }

    // Item class
    static class Item {
        private String id;
        private String name;
        private String category;
        private int quantity;

        public Item(String id, String name, String category, int quantity) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        @Override
        public String toString() {
            return "Item{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", category='" + category + '\'' +
                    ", quantity=" + quantity +
                    '}';
        }
    }

    public static void main(String[] args) {
        Main inventory = new Main();

        // Add items
        System.out.println("Add Items:");
        inventory.addOrUpdateItem("1", "Laptop", "Electronics", 50);
        inventory.addOrUpdateItem("2", "Chair", "Furniture", 20);
        inventory.addOrUpdateItem("3", "Apple", "Groceries", 5);

        // Update an item
        System.out.println("\nUpdating Items:");
        inventory.addOrUpdateItem("1", "Laptop", "Electronics", 10);

        // Remove an item
        System.out.println("\nRemoving Item:");
        inventory.removeItem("2");

        // Get items by category
        System.out.println("\nGetting items by category:");
        System.out.println("Electronics: " + inventory.getItemsByCategory("Electronics"));

        // Get top-k items
        System.out.println("\nTop 2 items: \n" + inventory.getTopKItems(2));

        // Merge inventories
        System.out.println("\nMerging inventories: ");
        Main otherInventory = new Main();
        otherInventory.addOrUpdateItem("4", "Table", "Furniture", 30);
        otherInventory.addOrUpdateItem("1", "Laptop", "Electronics", 60); // Higher quantity
        inventory.mergeInventory(otherInventory);

        // Check inventory after merge
        System.out.println("\nAfter merge:");
        System.out.println("Electronics: " + inventory.getItemsByCategory("Electronics")+"\n");
        System.out.println("Clothing: " + inventory.getItemsByCategory("Clothing")+"\n");
        System.out.println("Groceries: " + inventory.getItemsByCategory("Groceries")+"\n");
        System.out.println("Furniture: " + inventory.getItemsByCategory("Furniture"));
    }
}
