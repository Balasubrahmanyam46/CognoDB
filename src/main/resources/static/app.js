const API = {
  people: '/api/people',
  recommend: (name) => `/api/recommend-mentors/${encodeURIComponent(name)}`,
  chain: (from, to) => `/api/mentor-chain?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`
};

const el = (id) => document.getElementById(id);
const show = (id) => el(id).classList.remove('hidden');
const hide = (id) => el(id).classList.add('hidden');

async function loadPeople() {
  show('lookup-loading');
  hide('lookup-error');
  hide('person-select');
  try {
    const res = await fetch(API.people);
    if (!res.ok) throw new Error('Request failed');
    const people = await res.json();

    const personSelect = el('person-select');
    const chainFrom = el('chain-from');
    const chainTo = el('chain-to');
    [personSelect, chainFrom, chainTo].forEach(sel => sel.innerHTML = '');

    people.forEach(p => {
      personSelect.add(new Option(`${p.name} — ${p.title}`, p.name));
      chainFrom.add(new Option(p.name, p.name));
      chainTo.add(new Option(p.name, p.name));
    });

    hide('lookup-loading');
    show('person-select');

    personSelect.addEventListener('change', () => loadMentors(personSelect.value));
    if (people.length) loadMentors(people[0].name);
  } catch (err) {
    hide('lookup-loading');
    show('lookup-error');
  }
}

async function loadMentors(name) {
  hide('mentors-empty');
  hide('mentors-none');
  hide('mentors-error');
  show('mentors-loading');
  el('mentors-list').innerHTML = '';

  try {
    const res = await fetch(API.recommend(name));
    if (!res.ok) throw new Error('Request failed');
    const recs = await res.json();
    hide('mentors-loading');

    if (!recs.length) { show('mentors-none'); return; }

    const list = el('mentors-list');
    recs.forEach((r, i) => {
      const card = document.createElement('div');
      card.className = 'card';
      card.style.setProperty('--tilt', `${(i % 2 === 0 ? -1 : 1) * (1 + (i % 3))}deg`);
      card.innerHTML = `
        <h3>${r.name}</h3>
        <p>${r.title}</p>
        <span class="skill-tag">${r.skill} · ${r.level}</span>
      `;
      list.appendChild(card);
    });
  } catch (err) {
    hide('mentors-loading');
    show('mentors-error');
  }
}

async function findChain() {
  const from = el('chain-from').value;
  const to = el('chain-to').value;
  hide('chain-none');
  hide('chain-error');
  show('chain-loading');
  el('chain-result').innerHTML = '';

  try {
    const res = await fetch(API.chain(from, to));
    if (!res.ok) throw new Error('Request failed');
    const paths = await res.json();
    hide('chain-loading');

    if (!paths.length) { show('chain-none'); return; }

    const chain = paths[0].chain;
    const container = el('chain-result');
    chain.forEach((personName, i) => {
      const node = document.createElement('div');
      node.className = 'chain-node';
      node.textContent = personName;
      container.appendChild(node);
      if (i < chain.length - 1) {
        const thread = document.createElement('div');
        thread.className = 'chain-thread';
        container.appendChild(thread);
      }
    });
  } catch (err) {
    hide('chain-loading');
    show('chain-error');
  }
}

el('retry-people').addEventListener('click', loadPeople);
el('retry-mentors').addEventListener('click', () => loadMentors(el('person-select').value));
el('retry-chain').addEventListener('click', findChain);
el('chain-find').addEventListener('click', findChain);

loadPeople();