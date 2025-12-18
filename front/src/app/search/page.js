import { Suspense } from "react";
import SearchClient from "./searchClient";

export default function SearchPage() {
  return (
    <Suspense fallback={<div className="container py-5">로딩 중...</div>}>
      <SearchClient />
    </Suspense>
  );
}
