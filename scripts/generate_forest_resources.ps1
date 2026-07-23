$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$assets = Join-Path $root 'src/main/resources/assets/theaurorian2'
$data = Join-Path $root 'src/main/resources/data/theaurorian2'

function Write-Json($path, $value) {
    New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null
    $value | ConvertTo-Json -Depth 50 | Set-Content -LiteralPath $path -Encoding utf8
}

function Write-Tag($kind, $name, $values) {
    Write-Json (Join-Path $root "src/main/resources/data/minecraft/tags/$kind/$name.json") @{
        replace = $false
        values = $values
    }
}

function Simple-Loot($name) {
    Write-Json (Join-Path $data "loot_table/blocks/$name.json") @{
        type = 'minecraft:block'
        pools = @(@{
            rolls = 1.0
            bonus_rolls = 0.0
            conditions = @(@{condition = 'minecraft:survives_explosion'})
            entries = @(@{type = 'minecraft:item'; name = "theaurorian2:$name"})
        })
        random_sequence = "theaurorian2:blocks/$name"
    }
}

# Blockstates and models
Write-Json (Join-Path $assets 'blockstates/silent_tree_log.json') @{
    variants = @{
        'axis=x' = @{model = 'theaurorian2:block/silent_tree_log_x'}
        'axis=y' = @{model = 'theaurorian2:block/silent_tree_log_y'}
        'axis=z' = @{model = 'theaurorian2:block/silent_tree_log_z'}
    }
}
Write-Json (Join-Path $assets 'models/block/silent_tree_log.json') @{
    parent = 'minecraft:block/cube_column'
    textures = @{end = 'theaurorian2:block/silent_tree_log_top'; side = 'theaurorian2:block/silent_tree_log'}
}
foreach ($axis in @('x', 'y', 'z')) {
    Write-Json (Join-Path $assets "models/block/silent_tree_log_$axis.json") @{
        parent = "minecraft:block/cube_column_uv_locked_$axis"
        textures = @{end = 'theaurorian2:block/silent_tree_log_top'; side = 'theaurorian2:block/silent_tree_log'}
    }
}
Write-Json (Join-Path $assets 'items/silent_tree_log.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:block/silent_tree_log'}
}

Write-Json (Join-Path $assets 'blockstates/silent_tree_leaves.json') @{
    variants = @{'' = @{model = 'theaurorian2:block/silent_tree_leaves'}}
}
Write-Json (Join-Path $assets 'models/block/silent_tree_leaves.json') @{
    parent = 'minecraft:block/leaves'
    textures = @{all = 'theaurorian2:block/silent_tree_leaves'}
}
Write-Json (Join-Path $assets 'items/silent_tree_leaves.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:block/silent_tree_leaves'}
}

Write-Json (Join-Path $assets 'blockstates/silent_tree_sapling.json') @{
    variants = @{'' = @{model = 'theaurorian2:block/silent_tree_sapling'}}
}
Write-Json (Join-Path $assets 'models/block/silent_tree_sapling.json') @{
    parent = 'minecraft:block/cross'
    render_type = 'minecraft:cutout'
    textures = @{cross = 'theaurorian2:block/silent_tree_sapling'}
}
Write-Json (Join-Path $assets 'models/item/silent_tree_sapling.json') @{
    parent = 'minecraft:item/generated'
    textures = @{layer0 = 'theaurorian2:block/silent_tree_sapling'}
}
Write-Json (Join-Path $assets 'items/silent_tree_sapling.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:item/silent_tree_sapling'}
}

Write-Json (Join-Path $assets 'blockstates/curtain_tree_log.json') @{
    variants = @{
        'axis=x' = @{model = 'theaurorian2:block/curtain_tree_log_x'}
        'axis=y' = @{model = 'theaurorian2:block/curtain_tree_log_y'}
        'axis=z' = @{model = 'theaurorian2:block/curtain_tree_log_z'}
    }
}
Write-Json (Join-Path $assets 'models/block/curtain_tree_log.json') @{
    parent = 'minecraft:block/cube_column'
    textures = @{end = 'theaurorian2:block/curtain_tree_log_top'; side = 'theaurorian2:block/curtain_tree_log'}
}
foreach ($axis in @('x', 'y', 'z')) {
    Write-Json (Join-Path $assets "models/block/curtain_tree_log_$axis.json") @{
        parent = "minecraft:block/cube_column_uv_locked_$axis"
        textures = @{end = 'theaurorian2:block/curtain_tree_log_top'; side = 'theaurorian2:block/curtain_tree_log'}
    }
}
Write-Json (Join-Path $assets 'items/curtain_tree_log.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:block/curtain_tree_log'}
}

Write-Json (Join-Path $assets 'blockstates/curtain_tree_leaves.json') @{
    variants = @{'' = @{model = 'theaurorian2:block/curtain_tree_leaves'}}
}
Write-Json (Join-Path $assets 'models/block/curtain_tree_leaves.json') @{
    parent = 'minecraft:block/leaves'
    textures = @{all = 'theaurorian2:block/curtain_tree_leaves'}
}
Write-Json (Join-Path $assets 'items/curtain_tree_leaves.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:block/curtain_tree_leaves'}
}

Write-Json (Join-Path $assets 'blockstates/curtain_tree_sapling.json') @{
    variants = @{'' = @{model = 'theaurorian2:block/curtain_tree_sapling'}}
}
Write-Json (Join-Path $assets 'models/block/curtain_tree_sapling.json') @{
    parent = 'minecraft:block/cross'
    render_type = 'minecraft:cutout'
    textures = @{cross = 'theaurorian2:block/curtain_tree_sapling'}
}
Write-Json (Join-Path $assets 'models/item/curtain_tree_sapling.json') @{
    parent = 'minecraft:item/generated'
    textures = @{layer0 = 'theaurorian2:block/curtain_tree_sapling'}
}
Write-Json (Join-Path $assets 'items/curtain_tree_sapling.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:item/curtain_tree_sapling'}
}

Write-Json (Join-Path $assets 'blockstates/tall_wick_grass.json') @{
    variants = @{
        'half=lower' = @{model = 'theaurorian2:block/tall_wick_grass_lower'}
        'half=upper' = @{model = 'theaurorian2:block/tall_wick_grass_upper'}
    }
}
foreach ($half in @('lower', 'upper')) {
    Write-Json (Join-Path $assets "models/block/tall_wick_grass_$half.json") @{
        parent = 'minecraft:block/cross'
        render_type = 'minecraft:cutout'
        textures = @{cross = "theaurorian2:block/tall_wick_grass_$half"}
    }
}
Write-Json (Join-Path $assets 'models/item/tall_wick_grass.json') @{
    parent = 'minecraft:item/generated'
    textures = @{layer0 = 'theaurorian2:block/tall_wick_grass_upper'}
}
Write-Json (Join-Path $assets 'items/tall_wick_grass.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:item/tall_wick_grass'}
}
Write-Json (Join-Path $assets 'blockstates/indigo_mushroom.json') @{
    variants = @{'' = @{model = 'theaurorian2:block/indigo_mushroom'}}
}
Write-Json (Join-Path $assets 'models/block/indigo_mushroom.json') @{
    parent = 'minecraft:block/cross'
    render_type = 'minecraft:cutout'
    textures = @{cross = 'theaurorian2:block/indigo_mushroom'}
}
Write-Json (Join-Path $assets 'models/item/indigo_mushroom.json') @{
    parent = 'minecraft:item/generated'
    textures = @{layer0 = 'theaurorian2:item/indigo_mushroom'}
}
Write-Json (Join-Path $assets 'items/indigo_mushroom.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:item/indigo_mushroom'}
}

Write-Json (Join-Path $assets 'blockstates/blueberry_bush.json') @{
    variants = @{
        'age=0' = @{model = 'theaurorian2:block/blueberry_bush_stage0'}
        'age=1' = @{model = 'theaurorian2:block/blueberry_bush_stage1'}
        'age=2' = @{model = 'theaurorian2:block/blueberry_bush_stage2'}
        'age=3' = @{model = 'theaurorian2:block/blueberry_bush_stage3'}
    }
}
foreach ($stage in 0..3) {
    Write-Json (Join-Path $assets "models/block/blueberry_bush_stage$stage.json") @{
        parent = 'minecraft:block/cross'
        render_type = 'minecraft:cutout'
        textures = @{cross = "theaurorian2:block/blueberry_bush_stage$stage"}
    }
}
Write-Json (Join-Path $assets 'models/item/blueberry.json') @{
    parent = 'minecraft:item/generated'
    textures = @{layer0 = 'theaurorian2:item/blueberry'}
}
Write-Json (Join-Path $assets 'items/blueberry.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:item/blueberry'}
}

Write-Json (Join-Path $assets 'blockstates/silent_wood_stick.json') @{
    variants = @{
        'facing=east' = @{model = 'theaurorian2:block/silent_wood_stick'; y = 270}
        'facing=north' = @{model = 'theaurorian2:block/silent_wood_stick'; y = 180}
        'facing=south' = @{model = 'theaurorian2:block/silent_wood_stick'}
        'facing=west' = @{model = 'theaurorian2:block/silent_wood_stick'; y = 90}
    }
}
Write-Json (Join-Path $assets 'models/item/silent_wood_stick.json') @{
    parent = 'minecraft:item/generated'
    textures = @{layer0 = 'theaurorian2:item/silent_wood_stick'}
}
Write-Json (Join-Path $assets 'items/silent_wood_stick.json') @{
    model = @{type = 'minecraft:model'; model = 'theaurorian2:item/silent_wood_stick'}
}
Write-Json (Join-Path $assets 'particles/wick.json') @{
    textures = @(
        'theaurorian2:wick_01',
        'theaurorian2:wick_02',
        'theaurorian2:wick_03',
        'theaurorian2:wick_04',
        'theaurorian2:wick_05'
    )
}

# Loot tables
Simple-Loot 'silent_tree_log'
Simple-Loot 'silent_tree_sapling'
Simple-Loot 'curtain_tree_log'
Simple-Loot 'curtain_tree_sapling'
Simple-Loot 'indigo_mushroom'
Simple-Loot 'silent_wood_stick'

Write-Json (Join-Path $data 'loot_table/blocks/blueberry_bush.json') @{
    type = 'minecraft:block'
    functions = @(@{function = 'minecraft:explosion_decay'})
    pools = @(
        @{
            rolls = 1.0
            bonus_rolls = 0.0
            conditions = @(@{
                condition = 'minecraft:block_state_property'
                block = 'theaurorian2:blueberry_bush'
                properties = @{age = '3'}
            })
            entries = @(@{type = 'minecraft:item'; name = 'theaurorian2:blueberry'})
            functions = @(
                @{
                    function = 'minecraft:set_count'
                    add = $false
                    count = @{type = 'minecraft:uniform'; min = 2.0; max = 3.0}
                },
                @{
                    function = 'minecraft:apply_bonus'
                    enchantment = 'minecraft:fortune'
                    formula = 'minecraft:uniform_bonus_count'
                    parameters = @{bonusMultiplier = 1}
                }
            )
        },
        @{
            rolls = 1.0
            bonus_rolls = 0.0
            conditions = @(@{
                condition = 'minecraft:block_state_property'
                block = 'theaurorian2:blueberry_bush'
                properties = @{age = '2'}
            })
            entries = @(@{type = 'minecraft:item'; name = 'theaurorian2:blueberry'})
            functions = @(
                @{
                    function = 'minecraft:set_count'
                    add = $false
                    count = @{type = 'minecraft:uniform'; min = 1.0; max = 2.0}
                },
                @{
                    function = 'minecraft:apply_bonus'
                    enchantment = 'minecraft:fortune'
                    formula = 'minecraft:uniform_bonus_count'
                    parameters = @{bonusMultiplier = 1}
                }
            )
        }
    )
    random_sequence = 'theaurorian2:blocks/blueberry_bush'
}

$shears = @{condition = 'minecraft:match_tool'; predicate = @{items = 'minecraft:shears'}}
$silkTouch = @{
    condition = 'minecraft:match_tool'
    predicate = @{predicates = @{
        'minecraft:enchantments' = @(@{enchantments = 'minecraft:silk_touch'; levels = @{min = 1}})
    }}
}
$silkOrShears = @{condition = 'minecraft:any_of'; terms = @($shears, $silkTouch)}
Write-Json (Join-Path $data 'loot_table/blocks/silent_tree_leaves.json') @{
    type = 'minecraft:block'
    pools = @(
        @{
            rolls = 1.0
            bonus_rolls = 0.0
            entries = @(@{
                type = 'minecraft:alternatives'
                children = @(
                    @{
                        type = 'minecraft:item'
                        name = 'theaurorian2:silent_tree_leaves'
                        conditions = @($silkOrShears)
                    },
                    @{
                        type = 'minecraft:item'
                        name = 'theaurorian2:silent_tree_sapling'
                        conditions = @(
                            @{condition = 'minecraft:survives_explosion'},
                            @{
                                condition = 'minecraft:table_bonus'
                                enchantment = 'minecraft:fortune'
                                chances = @(0.05, 0.0625, 0.083333336, 0.1)
                            }
                        )
                    }
                )
            })
        },
        @{
            rolls = 1.0
            bonus_rolls = 0.0
            conditions = @(@{condition = 'minecraft:inverted'; term = $silkOrShears})
            entries = @(@{
                type = 'minecraft:item'
                name = 'minecraft:stick'
                conditions = @(@{
                    condition = 'minecraft:table_bonus'
                    enchantment = 'minecraft:fortune'
                    chances = @(0.02, 0.022222223, 0.025, 0.033333335, 0.1)
                })
                functions = @(
                    @{
                        function = 'minecraft:set_count'
                        add = $false
                        count = @{type = 'minecraft:uniform'; min = 1.0; max = 2.0}
                    },
                    @{function = 'minecraft:explosion_decay'}
                )
            })
        }
    )
    random_sequence = 'theaurorian2:blocks/silent_tree_leaves'
}
Write-Json (Join-Path $data 'loot_table/blocks/curtain_tree_leaves.json') @{
    type = 'minecraft:block'
    pools = @(
        @{
            rolls = 1.0
            bonus_rolls = 0.0
            entries = @(@{
                type = 'minecraft:alternatives'
                children = @(
                    @{
                        type = 'minecraft:item'
                        name = 'theaurorian2:curtain_tree_leaves'
                        conditions = @($silkOrShears)
                    },
                    @{
                        type = 'minecraft:item'
                        name = 'theaurorian2:curtain_tree_sapling'
                        conditions = @(
                            @{condition = 'minecraft:survives_explosion'},
                            @{
                                condition = 'minecraft:table_bonus'
                                enchantment = 'minecraft:fortune'
                                chances = @(0.05, 0.0625, 0.083333336, 0.1)
                            }
                        )
                    }
                )
            })
        },
        @{
            rolls = 1.0
            bonus_rolls = 0.0
            conditions = @(@{condition = 'minecraft:inverted'; term = $silkOrShears})
            entries = @(@{
                type = 'minecraft:item'
                name = 'minecraft:stick'
                conditions = @(@{
                    condition = 'minecraft:table_bonus'
                    enchantment = 'minecraft:fortune'
                    chances = @(0.02, 0.022222223, 0.025, 0.033333335, 0.1)
                })
                functions = @(
                    @{
                        function = 'minecraft:set_count'
                        add = $false
                        count = @{type = 'minecraft:uniform'; min = 1.0; max = 2.0}
                    },
                    @{function = 'minecraft:explosion_decay'}
                )
            })
        }
    )
    random_sequence = 'theaurorian2:blocks/curtain_tree_leaves'
}
Write-Json (Join-Path $data 'loot_table/blocks/tall_wick_grass.json') @{
    type = 'minecraft:block'
    pools = @(@{
        rolls = 1.0
        bonus_rolls = 0.0
        conditions = @(@{condition = 'minecraft:survives_explosion'})
        entries = @(@{
            type = 'minecraft:item'
            name = 'theaurorian2:tall_wick_grass'
            conditions = @(@{
                condition = 'minecraft:block_state_property'
                block = 'theaurorian2:tall_wick_grass'
                properties = @{half = 'lower'}
            })
        })
    })
    random_sequence = 'theaurorian2:blocks/tall_wick_grass'
}

# Vanilla behavior tags
$smallFlowers = @(
    'theaurorian2:petunia_plant',
    'theaurorian2:nebula_blossom_cluster',
    'theaurorian2:moon_frost_flower',
    'theaurorian2:void_candle_flower',
    'theaurorian2:lavender_plant'
)
$treeReplaceables = @(
    'theaurorian2:aurorian_grass',
    'theaurorian2:aurorian_grass_light',
    'theaurorian2:tall_aurorian_grass',
    'theaurorian2:tall_lavender_plant',
    'theaurorian2:tall_wick_grass',
    'theaurorian2:indigo_mushroom',
    'theaurorian2:blueberry_bush',
    'theaurorian2:silent_wood_stick'
) + $smallFlowers
Write-Tag 'block' 'logs' @('theaurorian2:silent_tree_log', 'theaurorian2:curtain_tree_log')
Write-Tag 'block' 'logs_that_burn' @('theaurorian2:silent_tree_log', 'theaurorian2:curtain_tree_log')
Write-Tag 'block' 'leaves' @('theaurorian2:silent_tree_leaves', 'theaurorian2:curtain_tree_leaves')
Write-Tag 'block' 'saplings' @('theaurorian2:silent_tree_sapling', 'theaurorian2:curtain_tree_sapling')
Write-Tag 'block' 'small_flowers' $smallFlowers
Write-Tag 'block' 'flowers' @('theaurorian2:tall_wick_grass', 'theaurorian2:tall_lavender_plant')
Write-Tag 'block' 'replaceable_by_trees' $treeReplaceables
Write-Tag 'block' 'mineable/axe' @('theaurorian2:silent_tree_log', 'theaurorian2:curtain_tree_log')
Write-Tag 'block' 'mineable/hoe' @('theaurorian2:silent_tree_leaves', 'theaurorian2:curtain_tree_leaves')
Write-Tag 'item' 'logs' @('theaurorian2:silent_tree_log', 'theaurorian2:curtain_tree_log')
Write-Tag 'item' 'logs_that_burn' @('theaurorian2:silent_tree_log', 'theaurorian2:curtain_tree_log')
Write-Tag 'item' 'leaves' @('theaurorian2:silent_tree_leaves', 'theaurorian2:curtain_tree_leaves')
Write-Tag 'item' 'saplings' @('theaurorian2:silent_tree_sapling', 'theaurorian2:curtain_tree_sapling')
Write-Tag 'item' 'small_flowers' $smallFlowers
Write-Tag 'item' 'flowers' @('theaurorian2:tall_wick_grass', 'theaurorian2:tall_lavender_plant')

# Natural generation and saplings share the original forked, cloud-crown tree feature.
Write-Json (Join-Path $data 'worldgen/configured_feature/silent_tree.json') @{
    type = 'theaurorian2:silent_tree'
    config = @{}
}
Write-Json (Join-Path $data 'worldgen/configured_feature/trees_aurorian_plains.json') @{
    type = 'minecraft:random_selector'
    config = @{
        default = @{feature = 'theaurorian2:silent_tree'; placement = @()}
        features = @(@{
            chance = 0.5
            feature = @{feature = 'theaurorian2:curtain_tree'; placement = @()}
        })
    }
}
Write-Json (Join-Path $data 'worldgen/placed_feature/trees_aurorian_plains.json') @{
    feature = 'theaurorian2:trees_aurorian_plains'
    placement = @(
        @{
            type = 'minecraft:count'
            count = @{
                type = 'minecraft:weighted_list'
                distribution = @(@{data = 0; weight = 99}, @{data = 1; weight = 1})
            }
        },
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:surface_water_depth_filter'; max_water_depth = 0},
        @{type = 'minecraft:heightmap'; heightmap = 'OCEAN_FLOOR'},
        @{
            type = 'minecraft:block_predicate_filter'
            predicate = @{
                type = 'minecraft:would_survive'
                state = @{Name = 'theaurorian2:silent_tree_sapling'; Properties = @{stage = '0'}}
            }
        },
        @{type = 'minecraft:biome'}
    )
}
Write-Json (Join-Path $data 'worldgen/configured_feature/trees_silent_wood_forest.json') @{
    type = 'minecraft:random_selector'
    config = @{
        default = @{feature = 'theaurorian2:silent_tree'; placement = @()}
        features = @(
            @{chance = 0.08; feature = 'theaurorian2:fallen_silent_tree'},
            @{
                chance = 0.1
                feature = @{feature = 'theaurorian2:curtain_tree'; placement = @()}
            }
        )
    }
}
Write-Json (Join-Path $data 'worldgen/placed_feature/trees_silent_wood_forest.json') @{
    feature = 'theaurorian2:trees_silent_wood_forest'
    placement = @(
        @{
            type = 'minecraft:count'
            count = @{
                type = 'minecraft:weighted_list'
                distribution = @(@{data = 2; weight = 9}, @{data = 3; weight = 1})
            }
        },
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:surface_water_depth_filter'; max_water_depth = 0},
        @{type = 'minecraft:heightmap'; heightmap = 'OCEAN_FLOOR'},
        @{type = 'minecraft:biome'},
        @{
            type = 'minecraft:block_predicate_filter'
            predicate = @{
                type = 'minecraft:would_survive'
                state = @{Name = 'theaurorian2:silent_tree_sapling'; Properties = @{stage = '0'}}
            }
        }
    )
}

# The custom feature builds an original layered crown with open gaps and hanging leaf curtains.
Write-Json (Join-Path $data 'worldgen/configured_feature/curtain_tree.json') @{
    type = 'theaurorian2:curtain_tree'
    config = @{}
}
Write-Json (Join-Path $data 'worldgen/configured_feature/trees_curtain_tree_forest.json') @{
    type = 'minecraft:random_selector'
    config = @{
        default = @{feature = 'theaurorian2:curtain_tree'; placement = @()}
        features = @(
            @{chance = 0.08; feature = 'theaurorian2:fallen_curtain_tree'},
            @{
                chance = 0.1
                feature = @{feature = 'theaurorian2:silent_tree'; placement = @()}
            }
        )
    }
}
Write-Json (Join-Path $data 'worldgen/placed_feature/trees_curtain_tree_forest.json') @{
    feature = 'theaurorian2:trees_curtain_tree_forest'
    placement = @(
        @{
            type = 'minecraft:count'
            count = @{
                type = 'minecraft:weighted_list'
                distribution = @(@{data = 2; weight = 9}, @{data = 3; weight = 1})
            }
        },
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:surface_water_depth_filter'; max_water_depth = 0},
        @{type = 'minecraft:heightmap'; heightmap = 'OCEAN_FLOOR'},
        @{type = 'minecraft:biome'},
        @{
            type = 'minecraft:block_predicate_filter'
            predicate = @{
                type = 'minecraft:would_survive'
                state = @{Name = 'theaurorian2:curtain_tree_sapling'; Properties = @{stage = '0'}}
            }
        }
    )
}

# The custom feature keeps native terrain fitting and decorators without its mandatory upright stump.
function Write-FallenTree($name, $log, $sapling, $minLength, $maxLength) {
    Write-Json (Join-Path $data "worldgen/configured_feature/$name.json") @{
        type = 'theaurorian2:fallen_log'
        config = @{
            trunk_provider = @{
                type = 'minecraft:simple_state_provider'
                state = @{Name = "theaurorian2:$log"; Properties = @{axis = 'y'}}
            }
            log_length = @{
                type = 'minecraft:uniform'
                min_inclusive = $minLength
                max_inclusive = $maxLength
            }
            stump_decorators = @()
            log_decorators = @(@{
                type = 'minecraft:attached_to_logs'
                block_provider = @{
                    type = 'minecraft:simple_state_provider'
                    state = @{Name = 'theaurorian2:indigo_mushroom'}
                }
                directions = @('up')
                probability = 0.1
            })
        }
    }
    Write-Json (Join-Path $data "worldgen/placed_feature/$name.json") @{
        feature = "theaurorian2:$name"
        placement = @(@{
            type = 'minecraft:block_predicate_filter'
            predicate = @{
                type = 'minecraft:would_survive'
                state = @{Name = "theaurorian2:$sapling"; Properties = @{stage = '0'}}
            }
        })
    }
}
Write-FallenTree 'fallen_silent_tree' 'silent_tree_log' 'silent_tree_sapling' 4 7
Write-FallenTree 'fallen_curtain_tree' 'curtain_tree_log' 'curtain_tree_sapling' 5 8

# Blueberries form occasional harvestable patches; fallen branches are isolated forest-floor details.
Write-Json (Join-Path $data 'worldgen/configured_feature/patch_blueberry_bush.json') @{
    type = 'minecraft:simple_block'
    config = @{
        to_place = @{
            type = 'minecraft:simple_state_provider'
            state = @{Name = 'theaurorian2:blueberry_bush'; Properties = @{age = '3'}}
        }
    }
}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_blueberry_bush.json') @{
    feature = 'theaurorian2:patch_blueberry_bush'
    placement = @(
        @{type = 'minecraft:rarity_filter'; chance = 16},
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:heightmap'; heightmap = 'WORLD_SURFACE_WG'},
        @{type = 'minecraft:biome'},
        @{type = 'minecraft:count'; count = 48},
        @{
            type = 'minecraft:random_offset'
            xz_spread = @{type = 'minecraft:trapezoid'; min = -7; max = 7; plateau = 0}
            y_spread = @{type = 'minecraft:trapezoid'; min = -3; max = 3; plateau = 0}
        },
        @{
            type = 'minecraft:block_predicate_filter'
            predicate = @{
                type = 'minecraft:all_of'
                predicates = @(
                    @{type = 'minecraft:matching_block_tag'; tag = 'minecraft:air'},
                    @{
                        type = 'minecraft:matching_blocks'
                        blocks = 'theaurorian2:aurorian_grass_block'
                        offset = @(0, -1, 0)
                    }
                )
            }
        }
    )
}

$branchStates = @()
foreach ($facing in @('north', 'east', 'south', 'west')) {
    $branchStates += @{
        data = @{Name = 'theaurorian2:silent_wood_stick'; Properties = @{facing = $facing}}
        weight = 1
    }
}
Write-Json (Join-Path $data 'worldgen/configured_feature/patch_forest_branches.json') @{
    type = 'minecraft:simple_block'
    config = @{to_place = @{type = 'minecraft:weighted_state_provider'; entries = $branchStates}}
}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_forest_branches.json') @{
    feature = 'theaurorian2:patch_forest_branches'
    placement = @(
        @{type = 'minecraft:rarity_filter'; chance = 3},
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:heightmap'; heightmap = 'WORLD_SURFACE_WG'},
        @{type = 'minecraft:biome'},
        @{type = 'minecraft:count'; count = 3},
        @{
            type = 'minecraft:random_offset'
            xz_spread = @{type = 'minecraft:trapezoid'; min = -7; max = 7; plateau = 0}
            y_spread = @{type = 'minecraft:trapezoid'; min = -3; max = 3; plateau = 0}
        },
        @{
            type = 'minecraft:block_predicate_filter'
            predicate = @{
                type = 'minecraft:all_of'
                predicates = @(
                    @{type = 'minecraft:matching_block_tag'; tag = 'minecraft:air'},
                    @{
                        type = 'minecraft:matching_blocks'
                        blocks = 'theaurorian2:aurorian_grass_block'
                        offset = @(0, -1, 0)
                    }
                )
            }
        }
    )
}

# Forest understory: half the plains grass, 30% of its flowers, plus wick grass.
function Forest-Placement($sourcePath, $count) {
    $placed = Get-Content -Raw -LiteralPath $sourcePath | ConvertFrom-Json
    foreach ($modifier in $placed.placement) {
        if ($modifier.type -eq 'minecraft:count') {
            $modifier.count = $count
        }
    }
    return $placed.placement
}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_silent_forest_grass.json') @{
    feature = 'theaurorian2:patch_aurorian_grass'
    placement = Forest-Placement (Join-Path $data 'worldgen/placed_feature/patch_aurorian_grass.json') 16
}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_silent_forest_tall_grass.json') @{
    feature = 'theaurorian2:patch_tall_aurorian_grass'
    placement = Forest-Placement (Join-Path $data 'worldgen/placed_feature/patch_tall_aurorian_grass.json') 48
}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_silent_forest_flowers.json') @{
    feature = 'theaurorian2:patch_aurorian_flowers'
    placement = Forest-Placement (Join-Path $data 'worldgen/placed_feature/patch_aurorian_flowers.json') 19
}

$wickStates = @()
for ($level = 0; $level -le 15; $level++) {
    $wickStates += @{
        data = @{
            Name = 'theaurorian2:tall_wick_grass'
            Properties = @{half = 'lower'; level = [string]$level}
        }
        weight = 1
    }
}
Write-Json (Join-Path $data 'worldgen/configured_feature/patch_tall_wick_grass.json') @{
    type = 'minecraft:simple_block'
    config = @{
        to_place = @{type = 'minecraft:weighted_state_provider'; entries = $wickStates}
    }
}
Write-Json (Join-Path $data 'worldgen/placed_feature/patch_tall_wick_grass.json') @{
    feature = 'theaurorian2:patch_tall_wick_grass'
    placement = @(
        @{type = 'minecraft:rarity_filter'; chance = 3},
        @{type = 'minecraft:in_square'},
        @{type = 'minecraft:heightmap'; heightmap = 'MOTION_BLOCKING'},
        @{type = 'minecraft:biome'},
        @{type = 'minecraft:count'; count = 6},
        @{
            type = 'minecraft:random_offset'
            xz_spread = @{type = 'minecraft:trapezoid'; min = -3; max = 3; plateau = 0}
            y_spread = @{type = 'minecraft:trapezoid'; min = -2; max = 2; plateau = 0}
        },
        @{
            type = 'minecraft:block_predicate_filter'
            predicate = @{
                type = 'minecraft:all_of'
                predicates = @(
                    @{type = 'minecraft:matching_block_tag'; tag = 'minecraft:air'},
                    @{
                        type = 'minecraft:matching_blocks'
                        blocks = 'theaurorian2:aurorian_grass_block'
                        offset = @(0, -1, 0)
                    }
                )
            }
        }
    )
}

# Full-sized custom trees are more conspicuous than vanilla oaks, so plains use a 1% attempt rate.
$plainsBiomePath = Join-Path $data 'worldgen/biome/aurorian_plains.json'
$plainsBiome = Get-Content -Raw -LiteralPath $plainsBiomePath | ConvertFrom-Json
$plainsFeatures = @($plainsBiome.features)
$plainsFeatures[9] = @(
    'theaurorian2:trees_aurorian_plains',
    'theaurorian2:patch_tall_aurorian_grass',
    'theaurorian2:patch_aurorian_flowers',
    'theaurorian2:patch_aurorian_grass'
)
$plainsBiome.features = $plainsFeatures
Write-Json $plainsBiomePath $plainsBiome

# Like vanilla sunflower plains, lavender plains keep plains generation and add a dominant flower patch.
$lavenderBiome = Get-Content -Raw -LiteralPath $plainsBiomePath | ConvertFrom-Json
$lavenderFeatures = @($lavenderBiome.features)
$lavenderFeatures[9] = @(
    'theaurorian2:trees_aurorian_plains',
    'theaurorian2:patch_tall_aurorian_grass',
    'theaurorian2:patch_lavender',
    'theaurorian2:patch_aurorian_flowers',
    'theaurorian2:patch_aurorian_grass'
)
$lavenderBiome.features = $lavenderFeatures
Write-Json (Join-Path $data 'worldgen/biome/lavender_plains.json') $lavenderBiome

# Inherit the plains environment and underground generation, replacing only vegetation.
$forestBiome = Get-Content -Raw -LiteralPath (Join-Path $data 'worldgen/biome/aurorian_plains.json') | ConvertFrom-Json
$forestFeatures = @($forestBiome.features)
$forestFeatures[9] = @(
    'theaurorian2:trees_silent_wood_forest',
    'theaurorian2:patch_blueberry_bush',
    'theaurorian2:patch_forest_branches',
    'theaurorian2:patch_tall_wick_grass',
    'theaurorian2:patch_silent_forest_tall_grass',
    'theaurorian2:patch_silent_forest_flowers',
    'theaurorian2:patch_silent_forest_grass'
)
$forestBiome.features = $forestFeatures
Write-Json (Join-Path $data 'worldgen/biome/silent_wood_forest.json') $forestBiome

$curtainForestBiome = Get-Content -Raw -LiteralPath (Join-Path $data 'worldgen/biome/aurorian_plains.json') | ConvertFrom-Json
$curtainForestFeatures = @($curtainForestBiome.features)
$curtainForestFeatures[9] = @(
    'theaurorian2:trees_curtain_tree_forest',
    'theaurorian2:patch_blueberry_bush',
    'theaurorian2:patch_forest_branches',
    'theaurorian2:patch_tall_wick_grass',
    'theaurorian2:patch_silent_forest_tall_grass',
    'theaurorian2:patch_silent_forest_flowers',
    'theaurorian2:patch_silent_forest_grass'
)
$curtainForestBiome.features = $curtainForestFeatures
Write-Json (Join-Path $data 'worldgen/biome/curtain_tree_forest.json') $curtainForestBiome

# Humidity noise makes broad, continuous regions instead of chunk-scale random patches.
$allClimate = @(-2.0, 2.0)
function Biome-Entry($biome, $humidity, $weirdness) {
    @{
        biome = $biome
        parameters = @{
            temperature = $allClimate
            humidity = $humidity
            continentalness = $allClimate
            erosion = $allClimate
            weirdness = $weirdness
            depth = $allClimate
            offset = 0.0
        }
    }
}
$aurorianGenerator = @{
    type = 'minecraft:noise'
    biome_source = @{
        type = 'minecraft:multi_noise'
        biomes = @(
            (Biome-Entry 'theaurorian2:aurorian_plains' @(-2.0, 0.2) @(-2.0, 0.6)),
            (Biome-Entry 'theaurorian2:lavender_plains' @(-2.0, 0.2) @(0.6, 2.0)),
            (Biome-Entry 'theaurorian2:silent_wood_forest' @(0.2, 2.0) @(-2.0, 0.0)),
            (Biome-Entry 'theaurorian2:curtain_tree_forest' @(0.2, 2.0) @(0.0, 2.0))
        )
    }
    settings = 'theaurorian2:the_aurorian'
}
Write-Json (Join-Path $data 'dimension/the_aurorian.json') @{
    type = 'minecraft:overworld'
    generator = $aurorianGenerator
}

# This preset exposes Aurorian generation through the vanilla "World Type" control.
Write-Json (Join-Path $data 'worldgen/world_preset/aurorian.json') @{
    dimensions = @{
        'minecraft:overworld' = @{
            type = 'minecraft:overworld'
            generator = $aurorianGenerator
        }
        'minecraft:the_end' = @{
            type = 'minecraft:the_end'
            generator = @{
                type = 'minecraft:noise'
                biome_source = @{type = 'minecraft:the_end'}
                settings = 'minecraft:end'
            }
        }
        'minecraft:the_nether' = @{
            type = 'minecraft:the_nether'
            generator = @{
                type = 'minecraft:noise'
                biome_source = @{type = 'minecraft:multi_noise'; preset = 'minecraft:nether'}
                settings = 'minecraft:nether'
            }
        }
    }
}
Write-Json (Join-Path $root 'src/main/resources/data/minecraft/tags/worldgen/world_preset/normal.json') @{
    replace = $false
    values = @('theaurorian2:aurorian')
}
