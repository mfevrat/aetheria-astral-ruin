# Changelog

## 2.4.0 — l’histoire mise en scène
- **Nouvelle cinématique d’introduction** : un vrai court-métrage en cinq scènes (≈ 42 s) au lieu d’un survol de caméra :
  - la pluie d’étoiles et l’impact de l’étoile géante ;
  - la naissance de la magie (cristaux, cercles runiques) ;
  - la forge du gardien (trois Anciens en armure Astrale, Valerius qui se relève et rugit) ;
  - sa chute (ciel rouge, ailes, foudre, éclipse) ;
  - le retour au monde avec le titre.
- **Théâtre côté client**, sans modifier le monde : dôme de nébuleuse peint (2048×1024) et sol gravé de runes, dessinés haut dans le ciel.
- **Acteurs** : les vrais modèles animés du mod (Valerius et ses animations d’apparition, de lévitation et de rage ; armor stands en armure Astrale tenant le bâton).
- **Mise en scène** : 21 plans de caméra avec coupes en fondu au noir, flash blanc, secousses, effets visuels, sons et narration.
- **Musique** d’ouverture recomposée sur 42 s, synchronisée au tick près avec les temps forts.

## 2.3.0 — Journal d’Aventure intégré
- **Journal d’Aventure** (touche J) : les 6 étapes de l’aventure, chacune avec description, indices concrets (recettes, emplacement, conseils de combat) et récompenses. Une étape n’est révélée qu’une fois la précédente accomplie.
- **Suivi de quête à l’écran** (en haut à gauche), désactivable avec l’option client `questTracker`.
- Deux nouvelles étapes et advancements : *Le Bâton de l’Archi-Mage* et *Le Sceau Astral*. Nouvel ordre : Éveil → Bâton → Invocateur → Sceau → Gardien → Armes Éveillées.
- Retrait du chapitre FTB Quests : FTB Quests n’est pas disponible pour Forge en 1.20.4. L’aventure ne dépend plus d’aucun mod de quêtes.
- La cinématique d’introduction mentionne désormais le journal.

## 2.2.0 — introduction de l’aventure
- **Cinématique d’introduction** à la première connexion de chaque joueur (≈ 18 s) :
  - caméra en six plans, du ciel jusqu’au joueur ;
  - narration en quatre lignes, en français et en anglais ;
  - titre AETHERIA et premier objectif ;
  - musique d’ouverture composée (19,5 s).
- Le joueur est protégé (Résistance V) pendant la séquence. La touche Sauter permet de la passer.
- Nouvelle commande `/aetheria intro` pour la rejouer, et option `adventureIntro` dans la config commune.
- L’état « introduction vue » est sauvegardé dans la progression du joueur et conservé après la mort.
- README : liste des mods nécessaires pour l’aventure complète.

## 2.1.3 — progression conservée après la mort
- Corrige la perte de l’arbre runique, des Éclats Astraux, des titres et des bonus de mana à la mort ou au retour de l’End. Forge invalide la capability de l’ancien joueur avant la copie, et la `LazyOptional` invalidée ne redevenait jamais valide : la copie ne trouvait aucune donnée. L’accès est maintenant recréé à la demande.
- Sauvegarde de secours : la progression est aussi enregistrée dans `PlayerPersisted`, que Forge recopie après la mort, et sert de restauration si la capability échoue.

## 2.1.2 — correctif de plantage
- Corrige le plantage « Rendering Particle » (`TextureSheetParticle` sans sprite) au lancement de la plupart des sorts : les braises (`ember`) et la fumée (`void_smoke`) recevaient leur texture seulement au premier tick, alors que Minecraft peut les dessiner avant. Toutes les particules du mod ont maintenant leur sprite dès leur création.

## 2.1.2 — correctif de plantage
- **Corrige le plantage au lancement de tous les sorts sauf Trait Astral** (« Rendering Particle », sprite null). Les braises et la fumée n’avaient pas de texture à leur création ; si Minecraft les dessinait avant leur premier tick, le jeu plantait. Toutes les particules reçoivent maintenant leur sprite dès leur création.

## 2.1.1 — robustesse
- Une erreur dans un sort n’arrête plus le jeu : le sort est interrompu, l’erreur est écrite dans `logs/latest.log` et résumée dans le chat (fichier et ligne en cause).
- Même protection côté client pour les cercles runiques, les effets visuels et les cinématiques.
- Protocole réseau en version 3 : un client et un serveur avec des versions différentes du mod sont refusés à la connexion au lieu de planter.

## 2.1.0 — armure Astrale ciselée
- **Armure Astrale entièrement remodelée** : 113 pièces au lieu d’une trentaine, avec UV par face et texture de 512 px.
  - Casque : visière lumineuse, bandeau et crête d’or, gemme frontale, garde-joues, protège-nuque, ailes ornementales, halo flottant.
  - Plastron : gravure runique lumineuse, cœur de cristal pulsant, col d’or, lames abdominales, ceinture à boucle, tabard, arête dorsale.
  - Épaulières à deux lames, liseré d’or, pointes ornementales et runes en orbite ; gantelets à manchette et rune.
  - Jambières : cuissots à lame articulée et rune, genouillères ornées, tassettes, grèves à écailles, filets d’or.
  - Solerets : embout et rebord d’or, ailettes de cheville, anneaux tournants.
- **Peinture « astrale »** : plaques nuit étoilée parcourues de constellations (certaines étoiles brillent dans le noir), filets incrustés, biseaux, rivets dorés, filigranes d’or.
- **Icônes d’inventaire** : générées à partir des vraies pièces portées, recentrées, avec la même texture et une animation propre.

## 2.0.0 — combat spectaculaire et boss cinématique
- **Moteur d’effets visuels** (paquet `SpawnVfxS2C`, rendu additif). Il propose 8 effets : arc de lame, éclair, onde, implosion, faisceau, pilier, éclat et chaîne.
- **Textures et particules** :
  - 8 textures d’effets peintes ;
  - 6 familles de particules animées, soit 37 sprites inédits ;
  - 14 icônes de sorts.
- **9 nouveaux sorts** : Pluie d’Étoiles, Lance Comète, Halo Solaire, Mines Runiques, Prison de Glyphes, Sceau de Gravité, Tempête de Croissants, Chaînes du Néant, Requiem d’Éclipse.
- **Les 5 sorts existants** reçoivent de nouveaux effets (arcs, éclairs, flares, glyphes).
- **Astral Judgement** : nouveau pilier texturé à trois couches, avec éclairs autour de l’impact.
- **Arbre runique** réorganisé en trois branches complètes (21 nœuds).
- **Valerius entièrement refait** :
  - modèle de 112 cubes avec UV par face et texture de 512 px ;
  - 14 animations ;
  - 11 attaques et rage finale ;
  - arène qui se referme ;
  - compte à rebours du Jugement Absolu.
- **Cinématiques** d’apparition, de phase 2 et de mort, avec caméra scénarisée, bandes noires et titres. Elles se passent avec Sauter et se désactivent dans la config client.
- **Nouvelles entités** : comète, halo solaire, prison de glyphes, puits gravitationnel, lame spectrale. Les frappes runiques gagnent l’étoile filante, la mine et le pilier.
- **8 nouveaux sons** : tranchant, tonnerre, chaînes, gravité, météore, mine, rugissement, impact.

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
