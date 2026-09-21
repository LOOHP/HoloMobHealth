/*
 * This file is part of HoloMobHealth2.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.holomobhealth.utils;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.Pet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class MyPetUtils {

    private static final MiniMessage SANITIZED_MINIMESSAGE = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.decorations(),
                    StandardTags.reset(),
                    StandardTags.gradient(),
                    StandardTags.rainbow()
            ))
            .build();

    public static boolean isMyPet(Entity entity) {
        return MyPetApi.getPlugin().getPetManager().getPetFromEntity(entity) != null;
    }

    public static Component getMyPetName(Entity entity) {
        Pet pet = MyPetApi.getPlugin().getPetManager().getPetFromEntity(entity);
        if (pet == null) {
            return null;
        }
        return SANITIZED_MINIMESSAGE.deserialize(pet.getPetName());
    }

    public static EntityType getMyPetEntityType(Entity entity) {
        Pet pet = MyPetApi.getPlugin().getPetManager().getPetFromEntity(entity);
        if (pet == null) {
            return entity.getType();
        }
        String typeName = pet.getPetType().getBukkitName();
        try {
            return EntityType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return EntityType.UNKNOWN;
        }
    }

}
