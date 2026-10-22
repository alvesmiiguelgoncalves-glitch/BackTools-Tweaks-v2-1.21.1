# Back Tool Tweak

Mod **client** (Fabric 1.21.1) que ajusta, **item por item**, a posicao, rotacao e escala dos itens que o **BackSlot** (e o BackSlot Addon) desenham no corpo. Feito para consertar as armas grandes do Antarchy.

## Item 1 e item 2
- **item1** = item nas **costas** (renderer de costas do BackSlot).
- **item2** = item na **cintura** (renderer de cintura do BackSlot).
- Cada item tem uma pose para o item 1. A pose do item 2 e **opcional**: se o item nao tiver `item2`, a cintura usa a pose do `item1`.

Cada pose tem: `x`, `y`, `z` (pixels, 1/16 de bloco), `rotX`, `rotY`, `rotZ` (graus), `scale` (0 = normal, 0.5 = 50% maior), `visible` e `mirror`.

## Um arquivo por mod
Em `config/backtooltweak/` (abre no Bloco de Notas):

```
config/backtooltweak/antarchy.json     <- ja vem com os itens do Antarchy
config/backtooltweak/outromod.json     <- criado sozinho quando voce adiciona um item desse mod
```

Formato:
```json
{
  "antarchy:battle_axe": {
    "item1": { "visible": true, "mirror": false, "x": 0.0, "y": -2.0, "z": 1.0, "rotX": 90.0, "rotY": 0.0, "rotZ": 0.0, "scale": -0.3 },
    "item2": null
  },
  "antarchy:big_bertha": {
    "item1": { "visible": true, "mirror": false, "x": 0.0, "y": 0.0, "z": 0.0, "rotX": 0.0, "rotY": 0.0, "rotZ": 0.0, "scale": 0.0 },
    "item2": { "visible": true, "mirror": false, "x": 3.0, "y": 5.0, "z": 0.0, "rotX": 0.0, "rotY": 0.0, "rotZ": 45.0, "scale": -0.5 }
  }
}
```
`"item2": null` = a cintura usa o item 1. Para dar uma pose propria na cintura, troque o `null` por um bloco igual ao do `item1`. Depois de editar o arquivo, use `/backtool reload` (ou reabra o jogo).

Para incluir itens de outro mod, use `/backtool add <item>` (ou segure o item e use `/backtool add`). O arquivo `config/backtooltweak/<mod>.json` e criado automaticamente. Tambem da pra criar o arquivo na mao com o mesmo formato.

Se existir um `config/backtooltweak.json` da versao 1.0.0, ele e convertido sozinho para os arquivos por mod (o antigo vira `backtooltweak.old.json`; as poses antigas viram o `item1`).

## Tela de config (Mod Menu, opcional)
Uma aba por mod; dentro, um grupo por item com **Item 1 (costas)** e **Item 2 (cintura)** (liga "Usar config propria na cintura" para ter pose propria). Salva ao clicar em Salvar.

## Comandos (ajuste ao vivo; use F5 para ver as costas)
```
/backtool list
/backtool add [item]                                   (sem argumento = item da mao principal)
/backtool remove <item>
/backtool show <item>
/backtool set <item> <item1|item2> <campo> <valor>     ex.: /backtool set antarchy:battle_axe item1 rotX 90
/backtool nudge <item> <item1|item2> <campo> <delta>   ex.: /backtool nudge antarchy:battle_axe item2 y -2
/backtool item2 <item> on|off                          (on = cria item2 copiando o item1; off = cintura volta a usar o item1)
/backtool reset <item> [item1|item2]                   (sem slot: zera o item1 e remove o item2)
/backtool reload
```
Campos: `x y z rotX rotY rotZ scale visible mirror` (visible/mirror: 0 ou 1). `set`/`nudge` em `item2` criam o item 2 sozinhos, copiando o item 1.

## Dependencias
Fabric Loader >= 0.16.5, Fabric API, Cloth Config >= 15.0.130, **BackSlot**. Opcionais: Mod Menu, BackSlot Addon.

## Compilar
**Pelo GitHub (mais facil):** suba este repositorio, abra a aba **Actions**, espere o workflow `build` terminar e baixe o artefato `backtooltweak-jar`. Use o jar que **nao** tem `-sources` no nome.

**No PC:** JDK 21 + Gradle 8.12+, e na raiz: `gradle build`. O jar sai em `build/libs/`.

## Limitacoes / nao testado
- **Escrito sem poder compilar nem rodar o jogo.** Pode haver erro pontual de compilacao ou de mixin.
- Assumi que o item 2 (cintura) e desenhado pelo `BeltSlotFeatureRenderer` do BackSlot e o item 1 pelo `BackToolFeatureRenderer`. Se algum item aparecer no slot trocado, avise.
- Os mixins usam nomes **intermediary** (`method_3233`, `class_742`...). Funciona no jogo normal; **nao rode `runClient` em ambiente de desenvolvimento.**
- Alguns modelos do Antarchy (`big_bertha`, `royal_guardian_sword`, `royal_assailant_battleaxe`) trazem no proprio JSON um deslocamento `head` de +45,25 e escala 2, aplicado depois do seu ajuste. Se a arma ficar muito alta, compense com `y` negativo.
- Os sinais dos eixos dependem da pose do BackSlot. Se um eixo parecer invertido, use valor negativo ou `mirror`.
