export class Card {
  id: string;
  name: string;
  image_uris: {
    small: string;
    normal: string;
    large: string;
    png: string;
    art_crop: string;
    border_crop: string;
  };
  mana_cost: string;
  cmc: string;
  type_line: string;
  oracle_text: string;
  power: string;
  toughness: string;
  colors: string[];
  color_identity: string[];
  keywords: string[];
  set_name: string;
  rarity: string;
  rulings_uri: string;

  constructor(id?: string) {
    this.id = id ? id : null;
    this.image_uris = {
      small: null, normal: null, large: null, png: null, art_crop: null, border_crop: null
    };
  }
}
