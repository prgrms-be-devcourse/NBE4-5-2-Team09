import { redirect } from "next/navigation";
import ClientPage from "@/app/ClientPage";

export default function Page() {
  redirect("/coin/KRW-EOS");

  return <ClientPage />;
}
