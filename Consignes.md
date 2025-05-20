**Date de rendu : 13 juin 2025 à 23h00**

### Consignes pour le fork
Les étapes à suivre par chaque équipe pour organiser correctement votre projet :

1. L'équipe devra être constituée de 2 étudiants issus du même groupe de TD (de préférence du même sous-groupe). Communiquez les noms à votre chargé de TD.
Le fork de votre équipe sera créé par les enseignants dans le groupe GitLab [IHM-JavaFX](https://gitlabinfo.iutmontp.univ-montp2.fr/ihm). Tous les autres forks que vous allez créer seront ignorés !

### Consignes générales
* Pour que le projet soit fini, vous devez implémenter correctement l'ensemble des classes et méthodes présentées sur le diagramme de classes.
* Dans vos classes, vous pouvez ajouter toutes les fonctions qui vous paraissent nécessaires. Pour ce qui est de la partie logique, si besoin, vous pouvez ajouter des fonctions utilitaires, mais cela ne devrait pas être nécessaire.
* Vous respecterez les bonnes pratiques en programmation objet vues jusque-là.
* Le respect des conventions de nommage du langage Java est **impératif**.
* Si vous souhaitez ajouter des tests unitaires, vous pouvez utiliser le framework [TestFX](https://github.com/TestFX/TestFX) qui permet de définir des tests sur une IHM en JavaFX, une dépendance sur une version du framework étant définie dans le pom du projet. Toutefois, nous préférons que vous n'alliez pas dans cette voie, car cela peut représenter un investissement non négligeable, et que nous n'avons pas eu l'occasion de voir cela pendant notre cours.
* Certaines précisions ou consignes pourront être ajoutées ultérieurement et vous en serez informés.
* Surveillez l'activité sur [le forum](https://piazza.com/class/m8snjyc99g2v4), les nouvelles informations y seront mentionnées. N'hésitez pas à y poser des questions, surtout si les réponses pourraient intéresser les autres équipes.

### Consignes pour la gestion de version
* Chaque commit devrait être accompagné d'un message permettant de comprendre l'objet de la modification.
* Vos _commits_ doivent être les plus petits possibles. Un commit qui fait 10 modifications de code sans lien entre elles, devra être découpé en 10 _commits_.
* Les _commits_ doivent être faits par l'auteur du code. Si vous avez travaillé à deux sur une fonctionnalité, il est impératif que chacun fasse ses propres _commits_. **Lors de la soutenance, l'auteur des différents _commits_ devra être capable de justifier ses choix.**
* On vous conseille d'utiliser des _branches Git_ différentes lors du développement d'une fonctionnalité importante. Le nom de la branche doit facilement permettre de comprendre la fonctionnalité qui est implémentée. Une fois que vous pensez que le code de la fonctionnalité est fini (tous les tests associés à celle-ci passent), vous fusionnerez le code de sa branche avec la branche `main`.