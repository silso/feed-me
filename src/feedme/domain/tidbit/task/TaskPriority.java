package feedme.domain.tidbit.task;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public enum TaskPriority {
    Critical("critical"),
    Major("major"),
    Minor("minor");

    private final BiMap<String, TaskPriority> map = HashBiMap.create();
    TaskPriority(String name) {
        this.map.put(name, this);
    }
}
