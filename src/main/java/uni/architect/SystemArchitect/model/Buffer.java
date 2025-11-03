package uni.architect.SystemArchitect.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class Buffer {
    private int capacity;
    private int currentSize;
    private Integer currentPriorityPackage = null;
    private Map<Integer, Deque<Request>> requestPackages;

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.currentSize = 0;
        requestPackages = new java.util.HashMap<>();
    }

    public boolean isFull() {
        return currentSize >= capacity;
    }

    public Request addRequest(Request request) {
        if (currentSize < capacity) {
            addRequestNoCheck(request);
            return null;
        }

        Request declined = request;
        List<Integer> genKeys = requestPackages.keySet().stream()
                .filter(k -> k > request.getGeneratorNumber())
                .sorted(Comparator.reverseOrder())
                .toList();
        for (int genKey : genKeys) {
            boolean isEmpty = requestPackages.get(genKey).isEmpty();
            if (isEmpty) {
                continue;
            }

            if (genKey > request.getGeneratorNumber()) {
                Deque<Request> deque = requestPackages.get(genKey);
                declined = deque.removeLast();
                currentSize--;
                if (deque.isEmpty()) {
                    requestPackages.remove(genKey);
                    recalculateCurrentPriorityPackage();
                }
                addRequestNoCheck(request);
                break;
            }
        }
        return declined;
    }

    private void addRequestNoCheck(Request request) {
        requestPackages.putIfAbsent(request.getGeneratorNumber(), new java.util.LinkedList<>());
        requestPackages.get(request.getGeneratorNumber()).add(request);
        currentSize++;
    }

    public Request getNextRequest() {
        if (currentSize == 0) {
            return null;
        }

        if (currentPriorityPackage == null) {
            recalculateCurrentPriorityPackage();
        }

        Deque<Request> queue = requestPackages.get(currentPriorityPackage);
        currentSize--;
        Request request = queue.removeFirst();
        if (queue.isEmpty()) {
            requestPackages.remove(currentPriorityPackage);
            recalculateCurrentPriorityPackage();
        }

        return request;
    }

    private void recalculateCurrentPriorityPackage() {
        currentPriorityPackage = null;
        List<Integer> genKeys = requestPackages.keySet().stream()
                .sorted()
                .toList();
        for (int i: genKeys) {
            if (requestPackages.get(i) != null && !requestPackages.get(i).isEmpty()) {
                currentPriorityPackage = i;
                break;
            }
        }

    }

    public String getState() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Integer key : requestPackages.keySet()) {
            sb.append(key + 1).append(": ");
            Deque<Request> queue = requestPackages.get(key);
            for (Request req : queue) {
                sb.append(req.getNumber()).append(", ");
            }
            if (!queue.isEmpty()) {
                sb.setLength(sb.length() - 2); // Remove last comma and space
                sb.append("; ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}
