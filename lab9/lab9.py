import itertools

A1 = {'a', 'b', 'c', 'd'}
I1 = [('a', 'd'), ('d', 'a'), ('b', 'c'), ('c', 'b')]
W1 = "baadcb"

A2 = {'a', 'b', 'c', 'd', 'e', 'f'}


class Graph(object):
    def __init__(self):
        self.V = {Node(0, 'a')}
        self.V.pop()
        self.E = {Edge(Node(0, 'a'), Node(1, 'b'))}
        self.E.pop()

    def add_node(self, v):
        self.V.add(v)

    def add_edge(self, e):
        self.E.add(e)

    def get_v(self):
        return self.V

    def get_e(self):
        return self.E

    def remove_edge(self, e):
        tmp = Edge
        for edge in self.get_e():
            if edge.get_nodes() == e.get_nodes():
                tmp = edge
        self.E.remove(tmp)

    def remove_node(self, v):
        tmp = Node
        for vertex in self.get_v():
            if vertex.ID == v.ID:
                tmp = vertex
        self.V.remove(tmp)


class Node(object):
    def __init__(self, v_id, label):
        self.ID = v_id
        self.Label = label

    def get_label(self):
        return self.Label


class Edge(object):
    def __init__(self, s_node, e_node):
        self.s_node = s_node
        self.e_node = e_node

    def get_nodes(self):
        return self.s_node, self.e_node

    def get_s_node(self):
        return self.s_node

    def get_e_node(self):
        return self.e_node


def get_dependency_relation(a, i):

    # różnica kombinacji liter i relacji niezależności
    d = []
    for (x, y) in itertools.product(a, a):
        if (x, y) not in i:
            d.append((x, y))
    d.sort()
    return d


def get_trace(i, w):

    t = {}
    t_next = {w}
    # dopóki iteracji nie zakończy się brakiem zmian wykonjemy operacje
    while t != t_next:
        t = t_next.copy()
        # dla każdego słowa w zbiorze spawdzamy kolejne 2 litery, jeśli są niezależne to zamieniamy ich miejsca i nowe słowo dodajemy do zbioru
        for word in t:
            for ind in range(len(word)-1):
                if (word[ind], word[ind+1]) in i:
                    new_word = word[:ind]+word[ind+1]+word[ind]+word[(ind+2):]
                    t_next.add(new_word)

    return t


def fnf(w, a, d):

    stacks = {}

    # tworzymy stosy dla poszczególnych liter
    for let in a:
        stacks[let] = []
    #  idąc od końca słowa, dodajemy literę do swojego stosu, a nastepnie na stosy liter zależnych od niej wrzucamy *
    for let in reversed(w):
        stacks[let].append(let)
        for (x, y) in d:
            if x != y and let == x:
                stacks[y].append('*')

    result = ""

    while True:
        # dopóki wszystkie stosy nie są puste wykonjemy pętle
        empty_stacks = 0
        for (let, stack) in stacks.items():
            if stack == []:
                empty_stacks += 1
        if empty_stacks == len(stacks):
            break
        result += '('
        # znajdujemy niepuste stosy posiadające literę u góry i sortujemy wyniki
        stack_with_letter_on_top = [let for let in a if stacks[let] != [] and stacks[let][-1] != '*']
        stack_with_letter_on_top.sort()
        # kolejno dla każdego ze stosów z literą na szczycie usuwamy ją, a następnie ze wszystkich stosów zależnych od naszego usuwamy * ze szczytu
        for let in stack_with_letter_on_top:
            if stacks[let][-1] != '*':
                letter = stacks[let][-1]
                stacks[let].pop(-1)
                result += letter
                for (x, y) in d:
                    if x == letter and y != x:
                        if stacks[y][-1] == '*':
                            stacks[y].pop(-1)
        result += ')'
    return result


def get_graph(w, d):

    g = Graph()

    nodes = {""}
    nodes.pop()

    # tworzymy graf zależności z nadmiarowymi krawędziami
    for let_ind in reversed(range(len(w))):
        v = Node(let_ind, w[let_ind])
        g.add_node(v)
        nodes.add(v)

        for node in nodes:
            if v != node:
                if (v.get_label(), node.get_label()) in d:
                    e = Edge(v, node)
                    g.add_edge(e)

    # usuwamy zbędne krawędzie
    for x in g.get_v():
        for y in g.get_v():
            for z in g.get_v():
                e = Edge(x, z)
                flag = False
                for edge in g.get_e():
                    if e.get_nodes() == edge.get_nodes():
                        for edge2 in g.get_e():
                            for edge3 in g.get_e():
                                    if edge2.get_nodes() == Edge(x, y).get_nodes() and edge3.get_nodes() == Edge(y, z).get_nodes():
                                        flag = True
                                        break
                            if flag:
                                break
                    if flag:
                        break
                if flag:
                    g.remove_edge(e)

    # tworzymy postac grafu służącą do wizualizacji
    result = "digraph g{\r\n"
    for v in g.get_v():
        label = v.get_label()
        result += str(v.ID)+"[label="+label+"]\r\n"
    for e in g.get_e():
        result += str(e.get_s_node().ID)+" -> "+str(e.get_e_node().ID)+"\n"
    result += "}"

    return result, g


def get_fnf_from_graph(g):
    g2 = g
    # lista wierzchołków nie będących końcami krawędzi
    current_nodes = []
    # fnf
    fnf_result = ""

    while True:
        # czyścimy liste po każdej iteracji
        current_nodes.clear()

        # dla każdego wierzchołka z grafu sprawdzamy czy jest końcem krawedzi, jeśli nie dodajemy go do naszej listy
        for v in g2.get_v():
            flag = False
            for e in g2.get_e():
                if e.get_e_node() == v:
                    flag = True
            if not flag:
                current_nodes.append(v)

        # każdy z wierzchołków w naszej liście dodajemy do stringa wynikowego
        if current_nodes != []:
            fnf_result += '('
            partial_res = ""
            for v in current_nodes:
                partial_res += v.get_label()

            fnf_result += ''.join(sorted(partial_res))
            fnf_result += ')'

        v_to_delete = []

        # dla wszystkich wierzchołków z listy usuwamy krawedzie, których wierzchołek jest początkiem, a sam wierzchołek dodajemy do listy do usunięcia
        for v in current_nodes:
            e_to_delete = []
            for e in g2.get_e():
                if e.get_s_node() == v:
                    e_to_delete.append(e)
            for e in e_to_delete:
                g2.remove_edge(e)

            v_to_delete.append(v)

        # usuwamy wierzchołki z listy do usunięcia
        for v in current_nodes:
            g2.remove_node(v)

        # pętle wykonujemy dopóki w grafie nie skończą się wierzchołki będącymi początkami krawędzi
        if current_nodes == []:
            break
    return fnf_result


def start(a, i, w):

    # Wyznacza relację zależności D
    d = get_dependency_relation(a, i)
    print("relacja zależności: ", d)
    # Wyznacza ślad[w] względem relacji I
    t = get_trace(i, w)
    print("ślad: ", t)
    # Wyznacza postać normalną Foaty FNF([w]) śladu[w]
    fnf_result = fnf(W1, A1, d)
    print("fnf: "+fnf_result)
    # Wyznacza graf zależności w postaci minimalnej dla słowa w
    graph, g = get_graph(w, d)
    print(graph)
    # Wyznacza postać normalną Foaty na podstawie grafu
    fnf_from_graph = get_fnf_from_graph(g)
    print("fnf_from_graph: "+fnf_from_graph)


start(A1, I1, W1)


# Dla danych
# A1 = {'a', 'b', 'c', 'd'}
# I1 = [('a', 'd'), ('d', 'a'), ('b', 'c'), ('c', 'b')]
# W1 = "baadcb"

# Wyniki
# relacja zależności:  [('a', 'a'), ('a', 'b'), ('a', 'c'), ('b', 'a'), ('b', 'b'), ('b', 'd'), ('c', 'a'), ('c', 'c'), ('c', 'd'), ('d', 'b'), ('d', 'c'), ('d', 'd')]
# ślad:  {'baadcb', 'baadbc', 'badabc', 'bdaabc', 'bdaacb', 'badacb'}
# fnf: (b)(ad)(a)(bc)
# digraph g{
# 2[label=a]
# 1[label=a]
# 3[label=d]
# 4[label=c]
# 5[label=b]
# 0[label=b]
# 0 -> 3
# 0 -> 1
# 2 -> 4
# 2 -> 5
# 1 -> 2
# 3 -> 4
# 3 -> 5
# }
# fnf_from_graph: (b)(ad)(a)(bc)