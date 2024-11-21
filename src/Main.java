import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        int maxCapacity = 10; // Максимальна місткість сховища
        int totalProduction = 50; // Загальна кількість продукції

        Storage storage = new Storage(maxCapacity);

        // Створення і запуск виробників
        int producerCount = 3;
        for (int i = 0; i < producerCount; i++) {
            new Thread(new Producer(storage, totalProduction / producerCount), "Producer-" + (i + 1)).start();
        }

        // Створення і запуск споживачів
        int consumerCount = 4;
        for (int i = 0; i < consumerCount; i++) {
            new Thread(new Consumer(storage), "Consumer-" + (i + 1)).start();
        }
    }
}

// Сховище з використанням семафорів
class Storage {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int capacity;

    // Семафори
    private final Semaphore accesslock = new Semaphore(1); // Доступ до критичної секції
    private final Semaphore items = new Semaphore(0); // Кількість продуктів
    private final Semaphore spaces; // Вільні місця в сховищі

    public Storage(int capacity) {
        this.capacity = capacity;
        this.spaces = new Semaphore(capacity);
    }

    public void produce(int product) throws InterruptedException {
        spaces.acquire(); // Зменшуємо кількість доступних місць
        accesslock.acquire();  // Входимо в критичну секцію

        queue.add(product); // Додаємо продукт у чергу
        System.out.println(Thread.currentThread().getName() + " produced: " + product);

        accesslock.release();  // Виходимо з критичної секції
        items.release();  // Збільшуємо кількість доступних продуктів
    }

    public int consume() throws InterruptedException {
        items.acquire(); // Зменшуємо кількість доступних продуктів
        accesslock.acquire(); // Входимо в критичну секцію

        int product = queue.poll(); // Забираємо продукт із черги
        System.out.println(Thread.currentThread().getName() + " consumed: " + product);

        accesslock.release(); // Виходимо з критичної секції
        spaces.release(); // Збільшуємо кількість доступних місць
        return product;
    }
}

// Клас виробника
class Producer implements Runnable {
    private final Storage storage;
    private final int totalToProduce;

    public Producer(Storage storage, int totalToProduce) {
        this.storage = storage;
        this.totalToProduce = totalToProduce;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < totalToProduce; i++) {
                storage.produce(i); // Виробляємо продукт
                Thread.sleep(100); // Імітація часу виробництва
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

// Клас споживача
class Consumer implements Runnable {
    private final Storage storage;

    public Consumer(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        try {
            while (true) {
                storage.consume(); // Споживаємо продукт
                Thread.sleep(150); // Імітація часу споживання
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
