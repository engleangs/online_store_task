package co.onlinestore.data;

import java.util.Comparator;
import java.util.Map;

public class ConversationComparator implements Comparator<Conversation> {

    @Override
    public int compare(Conversation o1, Conversation o2) {
        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
    }
}
