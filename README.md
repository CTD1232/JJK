# JujutsuCraft

Jujutsu Kaisen cursed energy, techniques, domain expansions and reverse cursed technique, all triggered by items.
Milestone 1: framework + Gojo and Sukuna. Milestone 2: Megumi, Nanami, Yuji, Nobara, Toji + resource pack.
Milestone 3: Yuta, Inumaki, Higuruma, Kenjaku.

## Build
`mvn package` -> `target/JujutsuCraft.jar`. If it fails to resolve the API, edit `paper.version` in `pom.xml`.
The GitHub Actions workflow publishes the jar and `JujutsuCraft-Pack.zip` to the `latest` release.

## Resource pack
`resourcepack/` holds 12 placeholder item textures keyed to `custom_model_data` 7001-7012 (matches `items:` in config.yml).
Host `JujutsuCraft-Pack.zip` and set `resource_pack.url` (and `sha1`) in config.yml to send it on join,
or set it in server.properties. If your client rejects the pack, adjust `pack_format` in `pack.mcmeta` to your version.

## Visuals (custom models)
Techniques spawn item_display entities using pack models (paper + custom_model_data 8001-8013), all cleaned up automatically:
- Blue / Red / Purple: glowing orbs (Purple is born from Blue and Red merging), Fuga: flame arrow
- Dismantle / Cleave: slash crescents, Black Flash: red-black bolts, Straw Doll nail: nail projectile
- Max: Elephant: a charging elephant, Mahoraga: a floating wheel that turns on every adaptation
- Rika: a model that follows an invisible Ravager body, Judgeman: floating gavel + courtroom ring, Uzumaki: spiral projectile
- Cursed Speech and verdicts: floating text (works without the pack)
- Unlimited Void: starfield room, Malevolent Shrine: the shrine model, Chimera Shadow Garden: shadow pool, Simple Domain: ground ring
Needs the pack on clients; set `visuals.enabled: false` to use particles/blocks only.

## Items and what they do
- gojo_blindfold: Blue, Red, Hollow Purple, Unlimited Void, Infinity (swap hands)
- sukuna_finger: Dismantle, Cleave, Fuga, Malevolent Shrine, Reverse Cursed Technique (swap hands)
- reversal_talisman: Reverse Cursed Technique, Simple Domain
- megumi_ten_shadows: Divine Dogs, Nue, Toad, Max: Elephant, Rabbit Escape, Chimera Shadow Garden
- mahoraga_wheel: summon Mahoraga (adapts to repeated damage types)
- nanami_cleaver: Ratio 7:3 strike, Overtime
- yuji_gauntlet: Black Flash (streaks raise the chance), Divergent Fist
- nobara_hammer: Nail (marks), Resonance (damages marked), Hairpin (explodes marked)
- yuta_katana: Rika (summon), Copy, Mimic, Authentic Mutual Love
- inumaki_seal: Stop!, Sleep!, Blast Away, Crush!, Twist! (each word costs a little HP, never lethal)
- higuruma_gavel: Deadly Sentencing (trial: techniques sealed; guilty = Executioner's Sword, not guilty = confiscation), Executioner's Sword, Confiscation
- kenjaku_ball: absorb hostile mobs, release them as summons, or compress them into Uzumaki
- heavenly_restriction_sigil: toggles Toji's Heavenly Restriction (no cursed energy, big physical boost, melee ignores Infinity)
- inverted_spear_of_heaven / split_soul_katana / playful_cloud: Toji's tools (no cursed energy needed)

## Commands (permission `jjk.admin`)
`/jjk give <item> [player]`, `/jjk energy <get|set|max|refill> [player] [amount]`, `/jjk cast <technique> [player]`, `/jjk list`, `/jjk reload`

## Customizing
Everything is in `config.yml`: costs, cooldowns, damage, radii, shells; rebind any technique to any trigger
(right_click, shift_right_click, left_click, shift_left_click, swap, shift_swap) on any item.
New techniques: extend `Technique`, register in `TechniqueRegistry`, add a config section.

## Note on `_stale/`
Five files (MarkManager, MeleeManager, SummonManager, SummonListener, MeleeListener) were found in the project that duplicate features
implemented elsewhere and broke the build. They were moved out of `src/` into `_stale/` (as .txt, not compiled). Safe to delete.
