export interface BackendPage {
  pageId: number;
  parentId?: number | null;
  name: string;
  url?: string | null;
  levelType?: number | null; // 0=一级菜单, 1=二级菜单
  levelIndex?: number | null;
  desc?: string | null;
}

export interface PageTreeNode {
  page: BackendPage;
  children: PageTreeNode[];
}

export function buildPageTree(pages: BackendPage[]): PageTreeNode[] {
  const safePages = (pages || []).filter(Boolean);
  const byId = new Map<number, PageTreeNode>();
  const roots: PageTreeNode[] = [];

  // init
  for (const p of safePages) {
    byId.set(p.pageId, { page: p, children: [] });
  }

  // attach
  for (const p of safePages) {
    const node = byId.get(p.pageId)!;
    const parentId = p.parentId ?? undefined;
    if (parentId && byId.has(parentId)) {
      byId.get(parentId)!.children.push(node);
    } else {
      roots.push(node);
    }
  }

  const sortNodes = (nodes: PageTreeNode[]) => {
    nodes.sort((a, b) => (a.page.levelIndex || 0) - (b.page.levelIndex || 0));
    for (const n of nodes) sortNodes(n.children);
  };
  sortNodes(roots);

  return roots;
}

export function collectDescendantIds(node: PageTreeNode): number[] {
  const out: number[] = [node.page.pageId];
  for (const c of node.children) out.push(...collectDescendantIds(c));
  return out;
}


