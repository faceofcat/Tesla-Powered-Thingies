#future - 1.13
- change mod id to 'thingies'
- change classes / namespaces to get rid of 'tesla'

#future
- add 'not-instant-first-run' machines (machines that start to fill the work buffer when a valid recipe is locked in)
- make 'power kiln' recipes take different power amounts 
- make all recipes take different amount of power
- replace 'crop farmer' with the 'misc farmer' machine via upgrades
- make simple/multi tanks behave like 1 big tank vertically (maybe cube size for simple tank)
- add a quarry machine
- add 'strategy' addons for pump and quarry
- add CT and API support to all non-farming machines (including the ones with Secondary Outputs, might be dependent on the support for that being added to TCL)
- add power cost to item liquefier recipes

#1.0.15
- fixed #42: Electric Butcher
- fixed #39: Butcher has no drops
- fixed #36: Powered Pump consumes Fluids

#1.0.14
- acacia trees should not leave blocks behind anymore
- tree farm now handles IC2 rubber trees
- fixed #33: Pump recipe error 
- fixed #21: Tree compat: Integrated Dynamics
- fixed ugly crash with pump on server side

#1.0.13
- simple and multi tanks now have fluid caps in item mode
- moved to MMD servers
- jar should now be signed
- removed a bit of log spam

#1.0.12
- new TCL version
- new forgelin

#1.0.11
- fixed crop cloner seed duplication bug
- added fluid pump machine
- added 'void storage' upgrade for tanks (keep accepting fluids even if full)
- added craft tweaker support to most machines
- added api support to most machines
- updated to latest forge
- fixed fluid solidifier jei recipe display

#1.0.10
- added support (tree farm) for forestry trees
- added support (crop farm) for rustic stake crops
- added 'misc farmer' machine (only farms based on addons)
    - 'tree fruit picker' addon only works with this machine now
    - added 'bush picker' addon that will work with this and harvest bushes from various mods (using the optional BushMasterCore lib)
- added 'compound maker' machine (to replace item compound producer)
    - supports 2 input fluid tanks... and 6 total input stacks
    - just 1 output slot! 
- first version of the 'possible' 6x ore processing
    - ore -\> teslified lump -\> augmented lump -\> powder -\> ingot
- new textures for teslified lumps and augmented lumps

#1.0.9 - HFs
- fixed some missing/broken recipes
- fixed problem with redstone fluid textures on server side

#1.0.9
- improved some GUIs (mainly about the order of the side config colors)
- added 'incinerator' custom recipes json
- added 'powered kiln' custom recipes json
- localize all the things!

#1.0.8
- added 'fluid compound producer' (combines fluids into other fluids)
- added config files for extra recipes to various machines
    - "{registry_name}-base.json" - this is the default recipes config generated by the mod
    - "{registry_name}-extra.json" - extra recipes file that is loaded, if it exists, but will never be created/overwrited by the mod
    - check "-base.json" files for examples of the recipe structure
- improved liquid xp storage machine 
- added 'player xp addon' - you put it into a 'liquid xp storage' and you can convert stored liquid xp to/from player xp
- fixed some of the default recipes for 'powder maker'

#1.0.7
- fixed 'powered kiln' recipes to support vanilla items
- 'item compound producer' is no longer WIP
- 'fluid solidifier' JEI screen got an update

