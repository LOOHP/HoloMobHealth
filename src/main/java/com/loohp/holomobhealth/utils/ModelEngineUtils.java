/*
 * This file is part of HoloMobHealth2.
 *
 * Copyright (C) 2023. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2023. Contributors
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

import com.ticxo.modelengine.ModelEngine;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.fake.NametagPoint;

import java.util.UUID;

public class ModelEngineUtils {

    public static void updateModelEngineNametags() {
        for (UUID uuid : ModelEngine.getModelTicker().getAllModeledEntityUUID()) {
            ModeledEntity modeledEntity = ModelEngine.getModeledEntity(uuid);
            for (ActiveModel activeModel : modeledEntity.getModels().values()) {
                for (NametagPoint nametagPoint : activeModel.getNametagHandler().getFakeEntity().values()) {
                    nametagPoint.setCustomNameVisible(false);
                    nametagPoint.update();
                }
            }
        }
    }

}
