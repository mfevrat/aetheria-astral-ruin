# Aetheria: Astral Ruin — v2.4.0

Mod Forge pour **Minecraft 1.20.4** (Forge 49.x) avec **GeckoLib 4.4.4**.
Magie cinématique à cercles runiques, trois sorts, équipement astral, un boss en deux phases et deux structures générées.

## Compiler et lancer

Prérequis : JDK 17 et une connexion Internet (Gradle télécharge Forge, Minecraft et GeckoLib).

```bash
./gradlew runClient     # lance le jeu de développement (Windows : gradlew.bat runClient)
./gradlew build         # produit build/libs/aetheria-2.4.0.jar
```

Pour jouer avec le jar : installe Forge 1.20.4 (49.0.27 ou plus) et **GeckoLib 4.4.4 pour Forge 1.20.4**, puis copie `aetheria-2.4.0.jar` dans `mods/`.

Dans IntelliJ IDEA : *Open* → choisis `build.gradle` → *Open as Project*, attends l’import, puis lance la configuration `runClient`.

> Ce projet a été écrit sans pouvoir être compilé dans l’environnement de génération (le dépôt Maven de Forge y était inaccessible). La syntaxe Java a été vérifiée avec `javac` et les signatures Forge/GeckoLib contrôlées dans leurs sources 1.20.4, mais il peut rester quelques erreurs de compilation mineures à corriger à la première compilation. Si tu en rencontres, colle le message d’erreur et je te donne le correctif.

## Mods à installer pour l’aventure complète

Tous en version **Minecraft 1.20.4, Forge**. Seuls trois éléments sont nécessaires :

| Mod | Rôle | Obligatoire |
| --- | --- | --- |
| Forge 49.x (49.0.27 ou plus) | Chargeur de mods | Oui |
| GeckoLib 4 (`geckolib-forge-1.20.4-4.4.4` ou plus) | Modèles et animations 3D | Oui |
| Aetheria: Astral Ruin | Le mod, journal de quêtes inclus | Oui |
| Just Enough Items (JEI) | Voir les recettes en jeu | Non, mais conseillé |

Aucun mod de quêtes externe n’est nécessaire : le **Journal d’Aventure** est intégré.

## Journal d’Aventure

La touche **J** ouvre le journal. Il liste les 6 étapes de l’aventure, chacune avec sa description, ses indices concrets (recettes, emplacement, astuces de combat) et ses récompenses. Un suivi en haut à gauche de l’écran rappelle l’étape en cours ; l’option `questTracker` de la config client permet de le masquer.

1. **L’Éveil des Runes** : trouver un Fragment de Stèle dans les Ruines Englouties.
2. **Le Bâton de l’Archi-Mage** : fabriquer le premier focus.
3. **La Voie de l’Invocateur** : débloquer des sorts et en lancer trois différents.
4. **Le Sceau Astral** : localiser l’Observatoire et fabriquer le Sceau.
5. **L’Épreuve du Gardien** : vaincre Valerius.
6. **L’Éveil des Armes** : forger une arme Éveillée.

Chaque étape est validée par un advancement du mod (visible aussi avec la touche L).

## Compiler sur GitHub (sans rien installer)

Le dépôt contient `.github/workflows/build.yml`. Dès que le projet est envoyé sur GitHub, l’onglet **Actions** compile le mod automatiquement.
- **Si la compilation réussit** : le jar est téléchargeable dans l’artefact `aetheria-mod`.
- **Si elle échoue** : le résumé de l’exécution liste les erreurs, prêtes à être copiées.

L’onglet Actions permet aussi de relancer la compilation à la main (*Run workflow*).

## Première compilation : à lire

Le mod a été écrit sans accès aux bibliothèques de Minecraft. Les vérifications faites :
- la syntaxe des 86 fichiers Java ;
- les imports, confrontés aux sources de Forge 1.20.4, GeckoLib 4.4.4 et Twilight Forest ;
- les signatures sensibles, relues dans les patchs Forge ;
- la cohérence de tous les fichiers de ressources.

Il peut malgré tout rester quelques erreurs de compilation, typiquement une méthode vanilla au nom ou aux paramètres légèrement différents.

1. Lance `./gradlew build` une première fois et laisse la configuration aller au bout (10 à 20 minutes au premier lancement).
2. S’il y a des erreurs, copie **tout le bloc `error:`** de la console et envoie-le-moi : je corrige fichier par fichier.
3. Une fois la compilation réussie, lance `./gradlew runClient`, crée un monde créatif et suis la section *Tester rapidement*.
4. Les points à regarder en premier en jeu :
   - l’orientation des objets en main (section `display` des modèles d’objet) ;
   - l’affichage des cercles en décalques (option `renderCircleDecals` pour comparer) ;
   - l’armure portée.

## Commandes de jeu

| Touche | Action |
| --- | --- |
| Clic droit (Bâton) ou **R** | Lancer le sort sélectionné |
| **V** (Maj+V = précédent) | Changer de sort |
| **G** ou clic droit (Grimoire) | Ouvrir l’arbre runique |

Les touches sont modifiables dans *Options → Commandes → Aetheria*.

## Progression

1. **L’Éveil des Runes** : explore les grottes (Y −20 à 20) pour trouver les **Ruines Englouties** et leur coffre, qui contient 1 à 2 **Fragments de Stèle Céleste**.
2. **La Voie de l’Invocateur** : craft le **Bâton de l’Archi-Mage** (lingots astraux, fragment, bâtons de blaze), débloque des sorts dans le **Grimoire**, lance 3 sorts différents.
3. **L’Épreuve du Gardien** : clic droit avec un Fragment pour localiser l’**Observatoire Obscur** (dans l’End ou au-dessus des pics de l’Overworld, à Y 262). Craft un **Sceau Astral** et utilise-le sur l’**Autel Astral** au centre.

Chaque quête accorde de la mana max et des Éclats Astraux, qui sont les points de l’arbre runique. Tu gagnes aussi 1 point par tranche de 500 mana dépensée.

## Contenu

- **Sorts (5)** :
  - *Barrière Runique* (branche Runique) : 35 mana, absorption et résistance, repousse les monstres.
  - *Pas d’Éclipse* (branche Éclipse) : 25 mana, bond de 8 blocs qui tranche tout sur son passage.
  - *Trait Astral* : 10 mana, débloqué d’office.
  - *Runework Nova* : 45 mana, 3 cercles verticaux, onde en cône et 8 cristaux explosifs.
  - *Astral Judgement* : 120 mana, cercle de 15 blocs dans le ciel puis pilier de lumière de 5 blocs pendant 5 s, avec désintégration des blocs fragiles.
- **Mana Astral** : régénération doublée hors combat. On peut lancer un sort jusqu’à −20 % de mana, au prix d’un contrecoup : dégâts, Faiblesse et régénération bloquée.
- **Faim d’Éclipse** (passif) : chaque coup de Lame d’Éclipse rend 3 mana.
- **Armes Éveillées**, forgées avec un Cœur de Runes Gravé :
  - Bâton Éveillé : +35 % de puissance, +200 mana, sorts 15 % moins chers.
  - Lame Éveillée : 11 dégâts, lame critique de 10 dégâts qui perce 4 cibles, +2 mana par coup.
- **Équipement** :
  - Bâton : +15 % de puissance, +100 mana.
  - Lame d’Éclipse : un coup critique projette une lame de losanges.
  - Armure Astrale : avec le set complet, −40 % de temps d’incantation et une aura runique.
  - Rempart d’Éclipse : renvoie 50 % des dégâts bloqués.
- **Valerius** (600 PV) :
  - Phase 1 : combo d’épée, pièges runiques et charge, qui l’étourdit s’il percute un pilier.
  - Transition à 50 % : 5 s d’invulnérabilité, cocon de rayons, puis onde de recul.
  - Phase 2 : il lévite et lance des météores et un faisceau qui suit sa cible, bloqué par les piliers. À 40 % puis à 20 %, il invoque 4 orbes qui le soignent. Si elles ne sont pas détruites en 30 s, le *Jugement Absolu* tue tout joueur qui n’est pas abrité derrière un pilier.
- **Butin** : Cœur de Runes Gravé (+100 mana max et 3 points), Rempart d’Éclipse (garanti au premier kill, 25 % ensuite), éclats, lingots et cristaux.

## Tester rapidement (mode créatif)

```
/give @s aetheria:archmage_staff
/give @s aetheria:astral_grimoire
/give @s aetheria:rune_heart 6          (points pour débloquer les sorts)
/give @s aetheria:awakened_archmage_staff
/give @s aetheria:astral_helmet         (+ chestplate, leggings, boots)
/summon aetheria:valerius ~ ~ ~5
/locate structure aetheria:dark_observatory_end
/locate structure aetheria:sunken_ruins
/place structure aetheria:dark_observatory_end
```

## Tout en 3D animé

Chaque objet du mod a son modèle GeckoLib animé en boucle, visible dans l’inventaire, en main, au sol et dans les cadres :

| Objet | Animation |
| --- | --- |
| Bâton de l’Archi-Mage | orbe qui tourne et pulse, deux anneaux en orbite sur deux axes, runes du manche qui battent |
| Lame d’Éclipse | runes de la lame qui pulsent, gemme de garde en rotation, éclats en orbite |
| Grimoire Astral | livre qui lévite, couverture qui s’entrouvre, rune flottante |
| Sceau Astral | gyroscope à trois anneaux sur trois axes, cœur pulsant |
| Cœur de Runes | double battement, runes en orbite |
| Cristal de mana | rotation, flottement, éclats en orbite inverse |
| Fragment de Stèle | runes lumineuses, éclat de pierre qui flotte |
| Éclat et Lingot astraux | rotation, bande runique pulsante, étincelle |
| Rempart d’Éclipse | emblème d’éclipse tournant sur la face avant |
| Armure Astrale (4 icônes + portée) | armure ciselée de 113 pièces (texture 512 px) : halo du casque, cœur de cristal qui pulse, runes en orbite autour des épaulières, anneaux aux solerets |
| Autel Astral (bloc et objet) | anneaux d’or et de rose en rotation, cristal flottant |
| Œuf de Valerius | œuf runique qui oscille, anneau en orbite |

Les projectiles (cristaux de Runework Nova, lames d’éclipse) et les orbes de soutien de Valerius utilisent automatiquement ces modèles.

Les fichiers sont dans `assets/aetheria/geo/`, `animations/` et `textures/geo/` (avec un `_glowmask.png` pour les parties lumineuses). Chaque modèle joue l’animation `animation.aetheria.idle` : tu peux tout retravailler dans Blockbench (plugin GeckoLib) sans toucher au Java, tant que ce nom d’animation est conservé.

## Cinématique d’introduction : l’histoire mise en scène

À sa première arrivée dans le monde, chaque joueur assiste à un court-métrage d’environ 42 secondes. Il est joué dans un théâtre dessiné côté client très haut dans le ciel (dôme de nébuleuse et sol gravé de runes), sans jamais modifier le monde.

1. **La pluie d’étoiles** : des dizaines d’étoiles filantes s’écrasent, puis une étoile géante frappe le centre dans un pilier de lumière.
2. **La naissance de la magie** : des cristaux flottent et s’éveillent, et des cercles runiques s’assemblent un à un.
3. **La forge du gardien** : trois Anciens en armure Astrale canalisent leurs rayons ; Valerius se relève et rugit.
4. **La chute** : le ciel vire au rouge, Valerius déploie ses ailes, la foudre frappe les Anciens et l’éclipse dévore tout.
5. **Le retour au monde** : flash blanc, la caméra redescend vers le joueur, titre AETHERIA et premier objectif.

Les acteurs sont les vrais modèles animés du mod. La séquence comprend des coupes en fondu, des secousses, des sons, une narration et une musique de 42 s calée sur chaque scène.

Le joueur est protégé pendant toute la séquence, et la touche *Sauter* permet de la passer. La commande `/aetheria intro` la rejoue. Elle se désactive pour tous avec `adventureIntro` (config commune) ou pour soi avec `bossCinematics` (config client).

## Combat et effets (v2)

### Moteur d’effets visuels

Tous les effets utilisent des textures peintes propres au mod, dans `textures/vfx/` et `textures/particle/` :
- arcs de lame ;
- éclairs à fourches régénérés en continu ;
- ondes de choc et implosions ;
- faisceaux et piliers aux textures qui défilent ;
- éclats lumineux ;
- chaînes attachées à deux entités ;
- trou noir translucide.

Six familles de particules animées complètent l’ensemble : étincelles, étoiles scintillantes, braises, fumée du néant, éclats de cristal qui rebondissent et glyphes runiques flottants.

### 14 sorts en 3 branches

| Branche | Sorts |
| --- | --- |
| Céleste | Trait Astral, Pluie d’Étoiles, Lance Comète, Halo Solaire, **Astral Judgement** (ultime) |
| Runique | Runework Nova, Barrière Runique, Mines Runiques, Prison de Glyphes, **Sceau de Gravité** (ultime) |
| Éclipse | Pas d’Éclipse, Tempête de Croissants, Chaînes du Néant, **Requiem d’Éclipse** (ultime) |

Chaque sort a son icône, affichée dans le HUD et dans le grimoire.

### Valerius retravaillé

**Le modèle** compte 112 cubes avec une texture de 512 px peinte en détail (biseaux, rivets, gravures lumineuses, plis de la cape). Il comprend :
- un casque à cornes et une visière lumineuse ;
- une couronne de runes flottantes ;
- une cape en trois segments animés ;
- un double halo ;
- des boucliers runiques en orbite ;
- des ailes de lumière en phase 2.

**Les animations** sont au nombre de 14.

**Les cinématiques** sont au nombre de trois, avec caméra scénarisée, bandes noires et titres :
- l’apparition, depuis le pilier de lumière jusqu’au rugissement ;
- le passage en Forme Libérée ;
- la mort.

Les déplacements et le HUD sont bloqués pendant ces séquences. La touche *Sauter* permet de les passer, et l’option `bossCinematics` de les désactiver.

**Les 11 attaques** se répartissent ainsi :
- Phase 1 :
  - combo de trois coups avec arcs de lame ;
  - saut écrasant, dont l’onde s’esquive en sautant ;
  - pièges runiques ;
  - charge d’Éclipse ;
  - lames spectrales qui visent puis fondent sur leur cible ;
  - tourbillon qui aspire.
- Phase 2 :
  - pluie de météores enflammés ;
  - faisceau continu texturé ;
  - croix de piliers de lumière ;
  - puits gravitationnel ;
  - pluie de jugement qui suit chaque joueur ;
  - lames spectrales ;
  - orbes de soutien.

**La rage** se déclenche à 15 % de PV : rugissement, attaques accélérées, et l’arène se referme. Rester hors du cercle brûle.

## Personnaliser par datapack (sans recompiler)

Toutes ces données se rechargent avec `/reload`.

- **Arbre runique** : un fichier par nœud dans `data/<namespace>/skill_nodes/<id>.json`. Le nom du fichier est l’identifiant ; un datapack peut modifier, ajouter ou remplacer des nœuds, et l’arbre est resynchronisé chez tous les joueurs.

  ```json
  { "x": 80, "y": -10, "cost": 1, "requires": ["astral_bolt"], "bonus": "spell_power", "amount": 0.1 }
  ```

  Les bonus possibles sont `max_mana`, `mana_regen`, `spell_power`, `backlash_resistance` et `none`. La clé `"spell"` débloque un sort (`astral_bolt`, `runework_nova` ou `astral_judgement`). Pensez à ajouter les clés de traduction `skill.aetheria.<id>` et `skill.aetheria.<id>.desc`.
- **Valerius** : `data/aetheria/bosses/valerius.json` règle PV, armure, vitesse, seuil de phase 2, durée de l’apparition, transition, rage (seuil, accélération, rayon de l’arène), orbes (seuils, PV, soin, délai) et chacune des 11 attaques (poids, portée, recharge, télégraphie, dégâts, rayon, nombre, durée, vitesse de rotation du faisceau). Une clé absente garde sa valeur par défaut, et un fichier invalide est signalé dans les logs puis ignoré.

## Configuration

- `config/aetheria-common.toml` : destruction de blocs par Judgement, blocs par tick, multiplicateur de PV du boss.
- `config/aetheria-client.toml` : intensité des secousses caméra (0 = aucune), densité des particules, affichage de l’aura, rendu des cercles en décalques, musique de boss.

## Arborescence

```
src/main/java/com/aetheria/astralruin/
  Aetheria.java            point d’entrée @Mod
  config/                  ForgeConfigSpec commun + client
  registry/                DeferredRegister (objets, blocs, entités, particules, sons, attributs, tags)
  mana/                    capability ManaData, régénération, synchro
  spell/                   AbstractSpell, CastingManager, impl/ (3 sorts)
  skill/                   arbre runique + chargeur JSON
  item/ block/             objets, armure, autel
  entity/spell/            pilier céleste, cristaux, lame, frappes runiques
  entity/boss/             Valerius (GeckoLib), orbes, définition JSON du boss
  particle/                options de particule et presets de cercles (partagés)
  network/                 canal Forge + 6 paquets
  event/                   événements communs (critique, bouclier, set, quêtes)
  util/Fx.java             effets visuels côté serveur
  client/                  particules, cercles 3D (décalques), caméra, rendus, HUD, grimoire, musique
src/main/resources/
  assets/aetheria/         lang FR/EN, modèles, textures, geo + animations, sons OGG
  data/aetheria/           recettes, loot tables, advancements, tags, worldgen, structures NBT,
                           skill_nodes/ (arbre runique), bosses/valerius.json
```

## Limites connues de la v0.3

- **Placement en main à ajuster** : les transformations d’affichage reprennent celles des objets vanilla (tenu en main, objet standard, bouclier, bloc), mais n’ont pas été vérifiées en jeu. Si un objet est mal orienté ou mal dimensionné, corrige la section `display` de `models/item/<objet>.json`, ou règle-la visuellement dans l’onglet *Display* de Blockbench.
- **Art provisoire** : modèles, textures, sons et musique sont générés procéduralement, à sculpter dans Blockbench pour un rendu final.
