/**
 * Stemwijzer (voting guide) questions data.
 * Each question has a category and subcategory for more specific matching.
 */

export const ANSWER_OPTIONS = {
  HELEMAAL_EENS: 'helemaal-eens',
  EENS: 'eens',
  NEUTRAAL: 'neutraal',
  ONEENS: 'oneens',
  HELEMAAL_ONEENS: 'helemaal-oneens'
}

export const ANSWER_SCORES = {
  [ANSWER_OPTIONS.HELEMAAL_EENS]: 2,
  [ANSWER_OPTIONS.EENS]: 1,
  [ANSWER_OPTIONS.NEUTRAAL]: 0,
  [ANSWER_OPTIONS.ONEENS]: -1,
  [ANSWER_OPTIONS.HELEMAAL_ONEENS]: -2
}

export const IMPORTANCE_LEVELS = {
  MINDER_BELANGRIJK: 'minder-belangrijk',
  BELANGRIJK: 'belangrijk',
  ZEER_BELANGRIJK: 'zeer-belangrijk'
}

export const IMPORTANCE_WEIGHTS = {
  [IMPORTANCE_LEVELS.MINDER_BELANGRIJK]: 0.5,
  [IMPORTANCE_LEVELS.BELANGRIJK]: 1.0,
  [IMPORTANCE_LEVELS.ZEER_BELANGRIJK]: 2.0
}

export const CATEGORIES = {
  KLIMAAT: 'Klimaat',
  IMMIGRATIE: 'Immigratie',
  ZORG: 'Zorg',
  WONINGMARKT: 'Woningmarkt',
  ECONOMIE: 'Economie',
  EUROPA: 'Europa',
  ONDERWIJS: 'Onderwijs',
  NATUUR: 'Natuur',
  VEILIGHEID: 'Veiligheid'
}

export const stemwijzerQuestions = [
  {
    id: 1,
    question: 'Nederland moet meer doen om klimaatverandering tegen te gaan, ook als dit hogere kosten betekent.',
    category: CATEGORIES.KLIMAAT,
    subcategory: 'Energie transitie'
  },
  {
    id: 2,
    question: 'Er moet een CO2-belasting komen voor vervuilende bedrijven.',
    category: CATEGORIES.KLIMAAT,
    subcategory: 'Industrie'
  },
  {
    id: 3,
    question: 'De immigratie naar Nederland moet worden beperkt.',
    category: CATEGORIES.IMMIGRATIE,
    subcategory: 'Algemeen'
  },
  {
    id: 4,
    question: 'Arbeidsmigranten van buiten de EU moeten makkelijker naar Nederland kunnen komen.',
    category: CATEGORIES.IMMIGRATIE,
    subcategory: 'Arbeidsmigratie'
  },
  {
    id: 5,
    question: 'De zorgkosten moeten worden verlaagd, ook als dit betekent dat sommige behandelingen niet meer worden vergoed.',
    category: CATEGORIES.ZORG,
    subcategory: 'Kosten'
  },
  {
    id: 6,
    question: 'De wachtlijsten in de GGZ moeten worden aangepakt, ook als dit extra belastinggeld kost.',
    category: CATEGORIES.ZORG,
    subcategory: 'Toegang'
  },
  {
    id: 7,
    question: 'Er moeten meer betaalbare woningen worden gebouwd, ook als dit betekent dat er minder ruimte is voor natuur.',
    category: CATEGORIES.WONINGMARKT,
    subcategory: 'Nieuwbouw'
  },
  {
    id: 8,
    question: 'Huisjesmelkers en beleggers moeten worden aangepakt om de woningmarkt eerlijker te maken.',
    category: CATEGORIES.WONINGMARKT,
    subcategory: 'Regulering'
  },
  {
    id: 9,
    question: 'Het minimumloon moet worden verhoogd.',
    category: CATEGORIES.ECONOMIE,
    subcategory: 'Lonen'
  },
  {
    id: 10,
    question: 'De belastingen voor bedrijven moeten worden verlaagd om de economie te stimuleren.',
    category: CATEGORIES.ECONOMIE,
    subcategory: 'Belastingen'
  },
  {
    id: 11,
    question: 'De AOW-leeftijd moet niet verder worden verhoogd.',
    category: CATEGORIES.ECONOMIE,
    subcategory: 'Pensioenen'
  },
  {
    id: 12,
    question: 'Nederland moet meer samenwerken met andere Europese landen.',
    category: CATEGORIES.EUROPA,
    subcategory: 'Samenwerking'
  },
  {
    id: 13,
    question: 'Nederland moet meer zeggenschap hebben over eigen beleid, ook als dit tegen EU-regels ingaat.',
    category: CATEGORIES.EUROPA,
    subcategory: 'Soevereiniteit'
  },
  {
    id: 14,
    question: 'Er moet meer geld naar het onderwijs, ook als dit betekent dat andere uitgaven moeten worden verlaagd.',
    category: CATEGORIES.ONDERWIJS,
    subcategory: 'Financiering'
  },
  {
    id: 15,
    question: 'Studenten moeten weer een basisbeurs krijgen.',
    category: CATEGORIES.ONDERWIJS,
    subcategory: 'Studiefinanciering'
  },
  {
    id: 16,
    question: 'Nederland moet meer doen om de biodiversiteit te beschermen.',
    category: CATEGORIES.NATUUR,
    subcategory: 'Biodiversiteit'
  },
  {
    id: 17,
    question: 'De intensieve veehouderij moet worden ingeperkt om stikstofuitstoot te verminderen.',
    category: CATEGORIES.NATUUR,
    subcategory: 'Landbouw'
  },
  {
    id: 18,
    question: 'Er moet meer worden geïnvesteerd in defensie en veiligheid.',
    category: CATEGORIES.VEILIGHEID,
    subcategory: 'Defensie'
  },
  {
    id: 19,
    question: 'De politie moet meer bevoegdheden krijgen om criminaliteit te bestrijden.',
    category: CATEGORIES.VEILIGHEID,
    subcategory: 'Politie'
  },
  {
    id: 20,
    question: 'Softdrugs zoals cannabis moeten volledig worden gelegaliseerd.',
    category: CATEGORIES.VEILIGHEID,
    subcategory: 'Drugs'
  }
]

export default stemwijzerQuestions
