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

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModelUpdaters;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import com.ticxo.modelengine.api.model.bone.type.NameTag;

import java.util.Optional;
import java.util.UUID;

public class ModelEngineUtils {

    public static void updateModelEngineNametags() {
        for (ModelUpdaters.Updater updater : ModelEngineAPI.getAPI().getModelUpdaters().getAvailable()) {
            for (UUID uuid : updater.getAllModeledEntityUUID()) {
                ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(uuid);
                for (ActiveModel activeModel : modeledEntity.getModels().values()) {
                    for (ModelBone modelBone : activeModel.getBones().values()) {
                        Optional<? extends NameTag> optNameTag = modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG);
                        if (optNameTag.isPresent()) {
                            NameTag nameTag = optNameTag.get();
                            nameTag.setVisible(false);
                        }
                    }
                }
            }
        }
    }

}
