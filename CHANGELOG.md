# Changelog

## 1.0.0 — contenu complet
- **Branche Runique** : sort *Barrière Runique* (absorption, résistance, onde qui repousse les monstres, 3 cercles verticaux).
- **Branche Éclipse** : passif *Faim d’Éclipse* (vol de mana au corps à corps) et sort *Pas d’Éclipse* (bond tranchant de 8 blocs).
- **Armes Éveillées** forgées avec le Cœur de Runes Gravé : Bâton Éveillé (sorts −15 % de mana) et Lame Éveillée, chacune avec son modèle 3D animé.
- Advancement *L’Éveil des Armes* (titre « Ascendant ») et 6ᵉ quête FTB.
- Logo du mod dans la liste des mods Forge.
- Vérification des imports contre les sources de Forge 1.20.4, GeckoLib 4.4.4 et Twilight Forest.

## 0.3.0
- **Tous les objets en 3D animé (GeckoLib)** : 17 objets avec modèle, texture, glowmask et animation en boucle, y compris les icônes des 4 pièces d’armure, l’objet de l’autel et l’œuf d’apparition.
- **Armure Astrale portée en 3D** (`GeoArmorRenderer`) : halo rotatif, runes d’épaulières en orbite, gemme pulsante, anneaux aux bottes.
- **Autel Astral animé** : devient un bloc à entité (`GeoBlockEntity`) avec anneaux et cristal en mouvement, y compris dans les structures générées.
- Le Rempart d’Éclipse passe du modèle vanilla au modèle GeckoLib (emblème tournant), avec la même pose de blocage.
- Projectiles et orbes de soutien rendus avec les nouveaux modèles.
- Correctif : les noms de modèles sont lus paresseusement, car Forge appelle `initializeClient` avant l’initialisation des champs des sous-classes d’`Item`.

## 0.2.0
- **Cercles runiques en décalques** : chaque anneau est un quad texturé en mélange additif, avec rotation et position interpolées au partial tick (rotation fluide, plus de saccades). Les particules ne servent plus qu’au scintillement (environ 70 % de particules en moins). Option `renderCircleDecals`.
- **Arbre runique data-driven** : nœuds dans `data/*/skill_nodes/*.json`, rechargés par `/reload` et synchronisés vers les clients (nouveau paquet `SkillTreeSyncS2C`).
- **Valerius data-driven** : `data/aetheria/bosses/valerius.json` pilote attributs, phases, orbes et les 5 attaques. Le boss applique sa définition dans `finalizeSpawn` (autel, `/summon`, œuf).
- **Rempart d’Éclipse en 3D** : rendu par `BlockEntityWithoutLevelRenderer` sur le modèle de bouclier vanilla, pose levée en blocage, runes émissives qui pulsent.
- **Musique de combat** : boucle de 48 s (ré mineur, chœur, taiko, carillons), démarre près de Valerius, monte légèrement en phase 2, s’éteint en fondu. Option `bossMusic`.
- **FTB Quests** : chapitre de 5 quêtes dans `extras/ftbquests/`.

## 0.1.0
- Première version : mana astral, 3 sorts, équipement, Valerius en deux phases, Observatoire et Ruines, quêtes en advancements.
