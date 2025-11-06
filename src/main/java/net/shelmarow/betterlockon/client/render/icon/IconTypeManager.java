package net.shelmarow.betterlockon.client.render.icon;

import net.shelmarow.betterlockon.client.render.icon.type.DefaultType;
import net.shelmarow.betterlockon.client.render.icon.type.IconType;
import net.shelmarow.betterlockon.client.render.icon.type.RPGType1;
import net.shelmarow.betterlockon.config.LockOnConfig;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IconTypeManager {
    private static final Set<IconType> ICON_TYPES = new HashSet<>(
            Set.of(new DefaultType(), new RPGType1())
    );

    @Nonnull
    public static IconType getIconTypeOrDefault(String name) {
        for (IconType type : ICON_TYPES) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return new DefaultType();
    }

    public static IconType getNextIconType() {
        if (ICON_TYPES.isEmpty()) {
            return new DefaultType();
        }
        IconType current = getIconTypeOrDefault(LockOnConfig.LOCK_ON_ICON_TYPES.get());
        List<IconType> iconTypes = ICON_TYPES.stream().toList();
        int index = 0;
        for (IconType type : iconTypes) {
            if (type.getName().equals(current.getName())) {
                break;
            }
            index++;
        }
        index = (index + 1) % iconTypes.size();
        return iconTypes.get(index);
    }

    public static void registerIconType(IconType iconType){
        ICON_TYPES.add(iconType);
    }

    public static boolean deleteIconType(IconType iconType){
        if(ICON_TYPES.contains(iconType)) {
            ICON_TYPES.remove(iconType);
            return true;
        }
        return false;
    }
}
